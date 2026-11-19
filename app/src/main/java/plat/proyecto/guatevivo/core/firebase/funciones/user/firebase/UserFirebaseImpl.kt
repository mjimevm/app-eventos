package plat.proyecto.guatevivo.core.firebase.funciones.user.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.publicProfile
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.normalizeProfileName

class UserFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : UserFirebase {
    private val users
        get() = firestore.collection(FirebaseCollections.USERS)
    private val profiles
        get() = firestore.collection(FirebaseCollections.PUBLIC_PROFILES)

    override suspend fun createUser(user: UserDto) {
        val currentUser = auth.currentUser
            ?: throw IllegalStateException("Usuario no autenticado")
        require(user.uid == currentUser.uid) { "El UID del perfil no coincide con la sesión autenticada" }
        require(user.accountType in listOf("USER", "BUSINESS")) { "Tipo de cuenta inválido" }
        val email = currentUser.email
            ?.trim()
            ?.lowercase(java.util.Locale.ROOT)
            ?: throw IllegalStateException("El usuario no tiene correo")
        val saved = user.copy(uid = currentUser.uid, email = email, nameNormalized = normalizeProfileName(user.name))
        firestore.batch().apply {
            set(users.document(currentUser.uid), saved)
            set(profiles.document(currentUser.uid), saved.publicProfile())
        }.commit().await()
    }

    override suspend fun getUser(uid: String): UserDto? {
        return users.document(uid).get().await().toObject(UserDto::class.java)
    }

    override suspend fun updateUser(user: UserDto) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        require(user.uid == uid) { "No puedes modificar el perfil de otro usuario" }

        firestore.runTransaction { transaction ->
            val existing = transaction.get(users.document(uid)).toObject(UserDto::class.java) ?: error("Perfil no encontrado")
            require(!existing.isDeleted) { "Esta cuenta fue eliminada" }
            val updated = existing.copy(
                uid = uid, name = user.name, phone = user.phone, address = user.address,
                description = user.description, photoUrl = user.photoUrl, nameNormalized = normalizeProfileName(user.name)
            )
            transaction.set(users.document(uid), updated)
            transaction.set(profiles.document(uid), updated.publicProfile())
        }.await()
    }

    override suspend fun deleteUser(uid: String) {
        val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        require(currentUid == uid) { "No puedes eliminar el perfil de otro usuario" }

        firestore.batch().apply {
            delete(users.document(uid))
            delete(profiles.document(uid))
        }.commit().await()
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
