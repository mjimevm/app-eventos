package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class FriendsFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    endpoint: () -> String
) : FriendsFirebase {
    private val emailClient = EmailFriendRequestClient(auth, endpoint)
    private val users get() = firestore.collection(FirebaseCollections.USERS)
    private val profiles get() = firestore.collection(FirebaseCollections.PUBLIC_PROFILES)
    private val requests get() = firestore.collection(FirebaseCollections.FRIEND_REQUESTS)
    private fun currentUid() = auth.currentUser?.uid ?: error("Debes iniciar sesión")

    override suspend fun ensureOwnProfile(): PublicProfileDto = syncOwnProfile()

    private suspend fun syncOwnProfile(): PublicProfileDto {
        val uid = currentUid()
        return firestore.runTransaction { transaction ->
            val source = transaction.get(users.document(uid))
            val directory = transaction.get(profiles.document(uid))
            val user = source.toObject(UserDto::class.java) ?: error("Perfil no encontrado")
            require(!user.isDeleted) { "Esta cuenta fue eliminada" }
            val profile = user.copy(uid = uid).publicProfile()
            val fields = mapOf(
                "nameNormalized" to profile.nameNormalized, "isDiscoverable" to profile.isDiscoverable,
                "isDeleted" to false, "photoUrl" to profile.photoUrl
            )
            if (fields.any { (key, value) -> source.get(key) != value }) transaction.update(users.document(uid), fields)
            if (directory.toObject(PublicProfileDto::class.java) != profile || directory.data?.size != 7)
                transaction.set(profiles.document(uid), profile)
            profile
        }.await()
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
            val base = requests.whereEqualTo(field, uid).whereIn("status", listOf("PENDING", "ACCEPTED"))
                .orderBy(FieldPath.documentId()).limit(50)
            var query = base
            do {
                val page = query.get(Source.SERVER).await()
                result += page.documents.mapNotNull { it.toObject(FriendRequestDto::class.java)?.copy(id = it.id) }
                val last = page.documents.lastOrNull()
                if (last != null) query = base.startAfter(last)
            } while (page.size() == 50)
        }
        return result.distinctBy { it.id }
    }

    override suspend fun sendFriendRequest(email: String): EmailFriendRequestOutcome {
        currentUid()
        return emailClient.send(email)
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
