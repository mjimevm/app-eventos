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
        val normalizedEmail = email.trim().lowercase(java.util.Locale.ROOT)
        val uid = try {
            authFirebase.register(normalizedEmail, password)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Error during Firebase Authentication registration.
            // Do not log credentials or passwords.
            throw RegistrationException("Error al registrar la cuenta en Authentication: ${registrationFailureDetails(e)}", e)
        }

        return saveRegistrationProfile(uid, userDto.copy(email = normalizedEmail))
    }

    // Retry only the failed profile write, for the same authenticated account.
    // Never retry createUserWithEmailAndPassword after Auth has already succeeded.
    suspend fun retryRegistrationProfile(uid: String, userDto: UserDto): String {
        check(authFirebase.getCurrentUserId() == uid &&
            authFirebase.getCurrentUserEmail()?.trim()?.lowercase(java.util.Locale.ROOT) == userDto.email) {
            "La sesión cambió. Inicia sesión con la cuenta pendiente antes de reparar el perfil."
        }
        return saveRegistrationProfile(uid, userDto)
    }

    private suspend fun saveRegistrationProfile(uid: String, userDto: UserDto): String {
        try {
            val userToCreate = userDto.copy(uid = uid)
            userFirebase.createUser(userToCreate)
            return uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Partial failure: Auth account created successfully, but Firestore profile creation failed.
            // Do not report false success. Do not delete the Auth account automatically.
            // Retain UID context for safe recovery handling by the UI/ViewModel.
            throw RegistrationException(
                "Cuenta creada en autenticación, pero falló al guardar el perfil en Firestore " +
                    "(UID parcial: $uid). Batch users/$uid + publicProfiles/$uid. " +
                    "${registrationFailureDetails(e)}. Reintenta guardar el perfil; no crees otra cuenta.",
                e, partialUid = uid
            )
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
