package plat.proyecto.guatevivo.paginas.profile.ui.friends

import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*

data class FriendsState(
    val uid: String = "",
    val relationships: List<FriendRequestDto> = emptyList(),
    val profiles: Map<String, PublicProfileDto> = emptyMap(),
    val email: String = "",
    val sendOutcome: EmailFriendRequestOutcome? = null,
    val sendError: String? = null,
    val isSending: Boolean = false,
    val selectedTab: Int = 0,
    val isLoading: Boolean = false,
    val processingUid: String? = null,
    val removeUid: String? = null,
    val error: String? = null,
    val openedProfile: PublicProfileDto? = null
) {
    fun otherUid(request: FriendRequestDto): String = if (request.senderUid == uid) request.receiverUid else request.senderUid
    fun relationship(other: String) = relationships.firstOrNull {
        (it.senderUid == other || it.receiverUid == other) && it.status == "ACCEPTED"
    } ?: relationships.firstOrNull { (it.senderUid == other || it.receiverUid == other) && it.status == "PENDING" }
    val friends get() = relationships.filter { it.status == "ACCEPTED" && profiles.containsKey(otherUid(it)) }.distinctBy { otherUid(it) }
    val requests get() = relationships.filter { it.status == "PENDING" && profiles.containsKey(otherUid(it)) }
}
