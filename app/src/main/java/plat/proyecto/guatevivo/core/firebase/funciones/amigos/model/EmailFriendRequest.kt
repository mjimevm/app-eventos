package plat.proyecto.guatevivo.core.firebase.funciones.amigos.model

import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.FirebaseNetworkException
import java.util.Locale

enum class EmailFriendRequestOutcome { SENT, PENDING, FRIENDS }

class FriendsActionException(val code: String, message: String, cause: Throwable? = null) : Exception(message, cause)

// Match the existing Auth email convention. Never strip accents, dots, plus tags or internal spaces.
fun normalizeFriendEmail(value: String): String = value.trim().lowercase(Locale.ROOT)

fun isValidFriendEmail(value: String): Boolean {
    val email = normalizeFriendEmail(value)
    if (email.length > 254 || email.count { it == '@' } != 1) return false
    val (local, domain) = email.split('@', limit = 2)
    if (local.length !in 1..64 || local.startsWith('.') || local.endsWith('.') || ".." in local ||
        !Regex("[a-z0-9!#$%&'*+/=?^_`{|}~.-]+").matches(local)) return false
    val labels = domain.split('.')
    return labels.size >= 2 && labels.last().length >= 2 && labels.last().any { it in 'a'..'z' } && labels.all {
        it.length in 1..63 && Regex("[a-z0-9](?:[a-z0-9-]*[a-z0-9])?").matches(it)
    }
}

fun friendsErrorCode(error: Exception): String = when (error) {
    is FriendsActionException -> error.code
    is FirebaseFirestoreException -> error.code.name
    is com.google.firebase.auth.FirebaseAuthException -> error.errorCode
    is FirebaseNetworkException -> "NETWORK_ERROR"
    else -> "UNKNOWN"
}

fun friendsFailureMessage(error: Exception): String {
    val code = when (error) {
        is FriendsActionException -> error.code
        is FirebaseFirestoreException -> error.code.name
        else -> null
    }
    if (code == "PERMISSION_DENIED") {
        val operation = (error as? FriendsActionException)?.message
            ?.takeIf { it.startsWith("PERFILES") || it.startsWith("friendRequests") }
            ?.substringBefore(": ")
        return if (operation != null)
            "PERMISSION_DENIED: Firebase denegó $operation. Revisa las reglas y el usuario autenticado."
        else "PERMISSION_DENIED: Firebase denegó la operación."
    }
    return listOfNotNull(code, error.localizedMessage).joinToString(": ").ifBlank { "No se pudo completar la operación" }
}
