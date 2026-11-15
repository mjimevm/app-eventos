package plat.proyecto.guatevivo.core.firebase.funciones.user.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class UserFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserFirebase {
    private val users
        get() = firestore.collection(FirebaseCollections.USERS)

    override suspend fun createUser(user: UserDto) {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("Usuario no autenticado")
        val email = currentUser.email
            ?.trim()
            ?.lowercase()
            ?: throw IllegalStateException("El usuario no tiene correo")
        users.document(currentUser.uid)
            .set(
                user.copy(
                    uid = currentUser.uid,
                    email = email
                )
            ).await()
    }

    override suspend fun getUser(uid: String): UserDto? {
        return users.document(uid).get().await().toObject(UserDto::class.java)
    }

    override suspend fun updateUser(user: UserDto) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        require(user.uid == uid) { "No puedes modificar el perfil de otro usuario" }

        users.document(uid)
            .update(
                mapOf(
                    "name" to user.name,
                    "phone" to user.phone,
                    "address" to user.address,
                    "description" to user.description
                )
            ).await()
    }

    override suspend fun deleteUser(uid: String) {
        val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        require(currentUid == uid) { "No puedes eliminar el perfil de otro usuario" }

        users.document(uid).delete().await()
    }

    override suspend fun getUserByEmail(
        email: String
    ): UserDto? {
        return users
            .whereEqualTo(
                "email",
                email.trim().lowercase()
            ).limit(1).get().await().toObjects(UserDto::class.java).firstOrNull()
    }
}