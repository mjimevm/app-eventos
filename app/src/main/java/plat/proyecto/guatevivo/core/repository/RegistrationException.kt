package plat.proyecto.guatevivo.core.repository

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException

class RegistrationException(
    message: String,
    cause: Exception,
    val partialUid: String? = null
) : IllegalStateException(message, cause)

internal fun registrationFailureDetails(error: Exception): String {
    val code = when (error) {
        is FirebaseAuthException -> error.errorCode
        is FirebaseFirestoreException -> error.code.name
        else -> error.javaClass.simpleName
    }
    return "$code: ${error.localizedMessage ?: "Sin detalle adicional"}"
}
