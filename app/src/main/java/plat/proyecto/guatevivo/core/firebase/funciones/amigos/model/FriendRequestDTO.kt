package plat.proyecto.guatevivo.core.firebase.funciones.amigos.model

data class FriendRequestDto(
    val id: String = "",
    val senderUid: String = "",
    val receiverUid: String = "",
    val senderName: String = "",
    val status: String = "PENDING",
    val updatedAt: Long = 0L
)
