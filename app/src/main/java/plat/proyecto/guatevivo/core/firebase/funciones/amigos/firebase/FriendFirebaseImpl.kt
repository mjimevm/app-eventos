package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class FriendsFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FriendsFirebase {
    private val users get() = firestore.collection(FirebaseCollections.USERS)
    private val profiles get() = firestore.collection(FirebaseCollections.PUBLIC_PROFILES)
    private val requests get() = firestore.collection(FirebaseCollections.FRIEND_REQUESTS)
    private fun currentUid() = auth.currentUser?.uid ?: error("Debes iniciar sesión")

    override suspend fun ensureOwnProfile(): PublicProfileDto = syncOwnProfile()

    private suspend fun syncOwnProfile(): PublicProfileDto {
        val uid = currentUid()
        val source = users.document(uid).get(Source.SERVER).await()
        val user = source.toObject(UserDto::class.java)
            ?: throw IllegalStateException("Perfil no encontrado en users/$uid")
        require(!user.isDeleted) { "Esta cuenta fue eliminada" }
        val profile = user.copy(uid = uid).publicProfile()

        val directoryReference = profiles.document(uid)
        val directory = directoryReference.get(Source.SERVER).await()
        val existing = directory.toObject(PublicProfileDto::class.java)?.copy(uid = directory.id)
        if (existing != profile || directory.data?.size != 7) {
            directoryReference.set(profile).await()
        }

        val fields = mapOf(
            "nameNormalized" to profile.nameNormalized,
            "isDiscoverable" to profile.isDiscoverable,
            "isDeleted" to false,
            "photoUrl" to profile.photoUrl
        )
        if (fields.any { (key, value) -> source.get(key) != value }) {
            users.document(uid).update(fields).await()
        }
        return profile
    }

    override suspend fun getProfile(uid: String): PublicProfileDto? {
        currentUid()
        val doc = profiles.document(uid).get(Source.SERVER).await()
        return doc.toObject(PublicProfileDto::class.java)?.copy(uid = doc.id)?.takeUnless { it.isDeleted }
    }

    override suspend fun getRelationships(): List<FriendRequestDto> {
        val uid = currentUid()
        val result = mutableListOf<FriendRequestDto>()
        for (field in listOf("senderUid", "receiverUid")) {
            val page = requests.whereEqualTo(field, uid)
                .whereIn("status", listOf("PENDING", "ACCEPTED"))
                .limit(50)
                .get(Source.SERVER)
                .await()
            result += page.documents.mapNotNull { it.toObject(FriendRequestDto::class.java)?.copy(id = it.id) }
        }
        return result.distinctBy { it.id }
    }

    override suspend fun sendFriendRequest(
        email: String
    ): EmailFriendRequestOutcome {

        val uid = currentUid()
        val normalizedEmail = normalizeFriendEmail(email)

        require(isValidFriendEmail(normalizedEmail)) {
            "Introduce un correo electrónico válido"
        }

        val receiverDocument = users
            .whereEqualTo("email", normalizedEmail)
            .limit(1)
            .get(Source.SERVER)
            .await()
            .documents
            .firstOrNull()
            ?: throw IllegalArgumentException(
                "No existe una cuenta con ese correo"
            )

        val receiverUid = receiverDocument.id

        require(receiverUid != uid) {
            "No puedes enviarte una solicitud a ti mismo"
        }

        val forwardRef = requests.document(
            friendRequestId(uid, receiverUid)
        )

        val reverseRef = requests.document(
            friendRequestId(receiverUid, uid)
        )

        val forward = forwardRef
            .get(Source.SERVER)
            .await()
            .toObject(FriendRequestDto::class.java)

        val reverse = reverseRef
            .get(Source.SERVER)
            .await()
            .toObject(FriendRequestDto::class.java)

        val existing = forward ?: reverse

        if (existing != null) {
            when (existing.status) {
                "ACCEPTED" -> return EmailFriendRequestOutcome.FRIENDS
                "PENDING" -> return EmailFriendRequestOutcome.PENDING
            }
        }

        forwardRef.set(
            mapOf(
                "id" to forwardRef.id,
                "senderUid" to uid,
                "receiverUid" to receiverUid,
                "status" to "PENDING",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()

        return EmailFriendRequestOutcome.SENT
    }

    override suspend fun acceptFriendRequest(requestId: String) = respond(requestId, "ACCEPTED")
    override suspend fun rejectFriendRequest(requestId: String) = respond(requestId, "REJECTED")

    private suspend fun respond(requestId: String, status: String) {
        val uid = currentUid()
        val reference = requests.document(requestId)
        firestore.runTransaction { transaction ->
            val request = transaction.get(reference).toObject(FriendRequestDto::class.java) ?: error("Solicitud no encontrada")
            FriendshipPolicy.canRespond(uid, request)
            val sender = transaction.get(profiles.document(request.senderUid)).toObject(PublicProfileDto::class.java)
            val receiver = transaction.get(profiles.document(uid)).toObject(PublicProfileDto::class.java)
            require(sender != null && receiver != null && !sender.isDeleted && !receiver.isDeleted) { "Esta cuenta ya no está disponible" }
            transaction.update(reference, mapOf("status" to status, "updatedAt" to System.currentTimeMillis()))
        }.await()
    }

    override suspend fun cancelFriendRequest(requestId: String) {
        val uid = currentUid()
        val reference = requests.document(requestId)
        firestore.runTransaction { transaction ->
            val request = transaction.get(reference).toObject(FriendRequestDto::class.java) ?: error("Solicitud no encontrada")
            FriendshipPolicy.canCancel(uid, request)
            transaction.delete(reference)
        }.await()
    }

    override suspend fun deleteFriend(friendUid: String) {
        val uid = currentUid()
        val forward = requests.document(friendRequestId(uid, friendUid))
        val reverse = requests.document(friendRequestId(friendUid, uid))
        firestore.runTransaction { transaction ->
            val records = listOf(forward, reverse).map { it to transaction.get(it).toObject(FriendRequestDto::class.java) }
            val accepted = records.filter { it.second?.status == "ACCEPTED" }
            require(accepted.isNotEmpty()) { "Esta amistad ya no existe" }
            accepted.forEach { transaction.delete(it.first) }
        }.await()
    }
}
