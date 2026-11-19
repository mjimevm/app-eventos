package plat.proyecto.guatevivo.core.firebase.funciones.amigos.model

import com.google.firebase.firestore.PropertyName
import java.security.MessageDigest
import java.text.Normalizer
import java.util.Locale
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

/** This projection deliberately contains no email, phone, address or private biography. */
data class PublicProfileDto(
    val uid: String = "",
    val name: String = "",
    val nameNormalized: String = "",
    val photoUrl: String = "",
    val accountType: String = "USER",
    @get:PropertyName("isDiscoverable")
    @field:PropertyName("isDiscoverable")
    val isDiscoverable: Boolean = false,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    val isDeleted: Boolean = false
)

fun normalizeProfileName(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT).trim().replace(Regex("[\\s\\p{Z}]+"), " ")

fun UserDto.publicProfile() = PublicProfileDto(
    uid = uid, name = name, nameNormalized = normalizeProfileName(name), photoUrl = photoUrl,
    accountType = accountType, isDiscoverable = isDiscoverable, isDeleted = isDeleted
)

/** Directed, unambiguous IDs. A transaction checks both directions before creating a request. */
fun friendRequestId(sender: String, receiver: String): String {
    require(sender.isNotBlank() && receiver.isNotBlank() && sender != receiver) { "Usuarios de solicitud no válidos" }
    fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return hash(sender) + hash(receiver)
}

object FriendshipPolicy {
    fun canRespond(uid: String, request: FriendRequestDto) {
        require(uid.isNotBlank() && request.receiverUid == uid && request.senderUid != uid) { "No puedes responder esta solicitud" }
        require(request.status == "PENDING") { "La solicitud ya fue procesada" }
    }

    fun canCancel(uid: String, request: FriendRequestDto) {
        require(uid.isNotBlank() && request.senderUid == uid && request.receiverUid != uid) { "No puedes cancelar esta solicitud" }
        require(request.status == "PENDING") { "La solicitud ya fue procesada" }
    }
}
