package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import kotlin.coroutines.cancellation.CancellationException

class AuthRepository(
    private val authFirebase: AuthFirebase,
    private val userFirebase: UserFirebase
) {
    suspend fun register(
        email: String,
        password: String,
        userDto: UserDto
    ): String {
        var uid = ""
        try {
            uid = authFirebase.register(email, password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Error during Firebase Authentication registration.
            // Do not log credentials or passwords.
            throw IllegalStateException("Error al registrar la cuenta en Authentication: ${e.localizedMessage}", e)
        }

        try {
            val userToCreate = userDto.copy(uid = uid, email = email.trim().lowercase())
            userFirebase.createUser(userToCreate)
            return uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Partial failure: Auth account created successfully, but Firestore profile creation failed.
            // Do not report false success. Do not delete the Auth account automatically.
            // Retain UID context for safe recovery handling by the UI/ViewModel.
            throw IllegalStateException("Cuenta creada en autenticación, pero falló al guardar el perfil en Firestore (UID parcial: $uid). Por favor, complete o repare su perfil.", e)
        }
    }

    suspend fun login(
        email: String,
        password: String
    ): String {
        return authFirebase.login(email, password)
    }

    suspend fun resetPassword(email: String) {
        authFirebase.resetPassword(email)
    }

    fun getCurrentUserId(): String? = authFirebase.getCurrentUserId()

    fun getCurrentUserEmail(): String? = authFirebase.getCurrentUserEmail()

    fun isLoggedIn(): Boolean = authFirebase.isLoggedIn()

    fun logout() {
        authFirebase.logout()
    }
}
