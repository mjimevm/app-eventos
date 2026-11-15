package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.FriendDto
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.FriendRequestDto
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class FriendsFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FriendsFirebase {
    private val users
        get() = firestore.collection(FirebaseCollections.USERS)
    private val requests
        get() = firestore.collection(FirebaseCollections.FRIEND_REQUESTS)
    private fun currentUid(): String {
        return auth.currentUser?.uid?: throw IllegalStateException("Usuario no autenticado")
    }

    private fun friends(uid: String) = users.document(uid).collection("friends")

    override suspend fun sendFriendRequest(email: String) {
        val uid = currentUid()
        val receiver = users
            .whereEqualTo("email", email.trim().lowercase())
            .limit(1)
            .get()
            .await()
            .toObjects(UserDto::class.java)
            .firstOrNull()
            ?: throw IllegalArgumentException("Usuario no encontrado")

        require(receiver.uid != uid) { "No puedes enviarte una solicitud a ti mismo" }
        require(!friends(uid).document(receiver.uid).get().await().exists()) { "Este usuario ya es tu amigo" }

        val existing = requests
            .whereEqualTo("senderUid", uid)
            .whereEqualTo("receiverUid", receiver.uid)
            .whereEqualTo("status", "PENDING")
            .get()
            .await()

        require(existing.isEmpty) { "Ya existe una solicitud pendiente" }

        val sender = users.document(uid)
            .get()
            .await()
            .toObject(UserDto::class.java)
            ?: throw IllegalStateException("Perfil no encontrado")

        val document = requests.document()

        document.set(
            FriendRequestDto(
                id = document.id,
                senderUid = uid,
                receiverUid = receiver.uid,
                senderName = sender.name,
                status = "PENDING"
            )
        ).await()
    }

    override suspend fun getPendingRequests(): List<FriendRequestDto> {
        return requests
            .whereEqualTo("receiverUid", currentUid())
            .whereEqualTo("status", "PENDING")
            .get()
            .await()
            .toObjects(FriendRequestDto::class.java)
    }

    override suspend fun acceptFriendRequest(requestId: String) {
        val uid = currentUid()

        val requestRef = requests.document(requestId)
        val request = requestRef.get().await()
            .toObject(FriendRequestDto::class.java)
            ?: throw IllegalArgumentException("Solicitud no encontrada")

        require(request.receiverUid == uid) { "No puedes aceptar esta solicitud" }

        require(request.status == "PENDING") { "La solicitud ya fue procesada" }

        val receiver = users.document(uid)
            .get()
            .await()
            .toObject(UserDto::class.java)
            ?: throw IllegalStateException("Perfil no encontrado")

        val batch = firestore.batch()

        batch.set(
            friends(uid).document(request.senderUid),
            FriendDto(
                uid = request.senderUid,
                name = request.senderName
            )
        )

        batch.set(
            friends(request.senderUid).document(uid),
            FriendDto(
                uid = uid,
                name = receiver.name
            )
        )

        batch.update(requestRef, "status", "ACCEPTED")
        batch.commit().await()
    }

    override suspend fun rejectFriendRequest(requestId: String) {
        val uid = currentUid()

        val requestRef = requests.document(requestId)
        val request = requestRef.get().await()
            .toObject(FriendRequestDto::class.java)
            ?: throw IllegalArgumentException("Solicitud no encontrada")

        require(request.receiverUid == uid) { "No puedes rechazar esta solicitud" }
        require(request.status == "PENDING") { "La solicitud ya fue procesada" }

        requestRef.update("status", "REJECTED").await()
    }

    override suspend fun getFriends(): List<FriendDto> {
        return friends(currentUid())
            .get()
            .await()
            .toObjects(FriendDto::class.java)
    }

    override suspend fun deleteFriend(friendUid: String) {
        val uid = currentUid()
        require(friendUid != uid) { "No puedes eliminarte a ti mismo" }
        val batch = firestore.batch()

        batch.delete(friends(uid).document(friendUid))
        batch.delete(friends(friendUid).document(uid))

        batch.commit().await()
    }
}