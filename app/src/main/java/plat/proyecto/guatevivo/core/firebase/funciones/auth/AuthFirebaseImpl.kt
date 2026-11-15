package plat.proyecto.guatevivo.core.firebase.funciones.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthFirebaseImpl(
    private val auth: FirebaseAuth
) : AuthFirebase {

    override suspend fun register(
        email: String,
        password: String
    ): String {

        val result = auth.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        return requireNotNull(result.user?.uid) {
            "No se pudo obtener el UID del usuario"
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): String {

        val result = auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        return requireNotNull(result.user?.uid) {
            "No se pudo obtener el UID del usuario"
        }
    }

    override suspend fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(
            email.trim()
        ).await()
    }

    override fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun isLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    override fun logout() {
        auth.signOut()
    }
}