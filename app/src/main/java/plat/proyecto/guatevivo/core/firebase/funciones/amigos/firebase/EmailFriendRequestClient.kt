package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import com.google.firebase.auth.FirebaseAuth
import java.net.HttpURLConnection
import java.net.URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*

/** Authenticated HTTPS transport, using existing dependencies; no Admin SDK or user queries on Android. */
class EmailFriendRequestClient(private val auth: FirebaseAuth, private val endpoint: () -> String) {
    suspend fun send(email: String): EmailFriendRequestOutcome {
        val uri = configuredFriendEndpoint(endpoint())
        val user = auth.currentUser ?: throw FriendsActionException("UNAUTHENTICATED", "Debes iniciar sesión")
        val token = user.getIdToken(false).await().token
            ?: throw FriendsActionException("UNAUTHENTICATED", "No se pudo obtener el token de sesión")
        if (auth.currentUser?.uid != user.uid) throw FriendsActionException("UNAUTHENTICATED", "La sesión cambió")
        return withContext(Dispatchers.IO) {
            val connection = uri.toURL().openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "POST"
                connection.instanceFollowRedirects = false // Never forward a Firebase token to another endpoint.
                connection.connectTimeout = 15000
                connection.readTimeout = 30000
                connection.doOutput = true
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                val payload = buildJsonObject { put("email", normalizeFriendEmail(email)) }.toString()
                connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload) }
                val status = connection.responseCode
                val stream = if (status in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                parseEmailFriendResponse(status, body)
            } finally { connection.disconnect() }
        }
    }
}

internal fun configuredFriendEndpoint(value: String): URI {
    val uri = runCatching { URI(value.trim()) }.getOrNull()
    val host = uri?.host.orEmpty()
    if (uri == null || uri.scheme != "https" || uri.userInfo != null || uri.query != null || uri.fragment != null ||
        !(host.endsWith(".cloudfunctions.net") || host.endsWith(".run.app"))) {
        throw FriendsActionException("FAILED_PRECONDITION", "El servicio de solicitudes no está configurado. Revisa la configuración de Firebase.")
    }
    return uri
}

internal fun parseEmailFriendResponse(status: Int, body: String): EmailFriendRequestOutcome {
    val root = runCatching { Json.parseToJsonElement(body) as? JsonObject }.getOrNull()
    val error = root?.get("error") as? JsonObject
    if (status !in 200..299 || error != null) {
        val code = when (status) {
            401 -> "UNAUTHENTICATED"
            403 -> "PERMISSION_DENIED"
            404 -> "NOT_FOUND"
            429 -> "RESOURCE_EXHAUSTED"
            else -> (error?.get("status") as? JsonPrimitive)?.contentOrNull ?: "HTTP_$status"
        }
        val message = (error?.get("message") as? JsonPrimitive)?.contentOrNull
            ?: "No se pudo enviar la solicitud. Comprueba el despliegue y los permisos de Firebase."
        throw FriendsActionException(code, message)
    }
    val result = root?.get("result") as? JsonObject
    val outcome = (result?.get("outcome") as? JsonPrimitive)?.contentOrNull
    return EmailFriendRequestOutcome.entries.firstOrNull { it.name == outcome }
        ?: throw FriendsActionException("INVALID_RESPONSE", "El backend no confirmó el resultado de la solicitud")
}
