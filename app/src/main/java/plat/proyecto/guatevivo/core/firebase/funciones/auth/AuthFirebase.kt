package plat.proyecto.guatevivo.core.firebase.funciones.auth

interface AuthFirebase {
    suspend fun register(
        email: String,
        password: String
    ): String

    suspend fun login(
        email: String,
        password: String
    ): String

    suspend fun resetPassword(email: String)
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun isLoggedIn(): Boolean
    fun logout()
}