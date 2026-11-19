package plat.proyecto.guatevivo.paginas.profile.ui.friends

sealed interface FriendsEvent {
    data class UpdateEmail(val email: String) : FriendsEvent
    data object SendRequest : FriendsEvent
    data class Accept(val requestId: String) : FriendsEvent
    data class Reject(val requestId: String) : FriendsEvent
    data class Cancel(val requestId: String) : FriendsEvent
    data class Remove(val uid: String) : FriendsEvent
    data object ConfirmRemove : FriendsEvent
    data object CancelRemove : FriendsEvent
    data class SelectTab(val index: Int) : FriendsEvent
    data object Refresh : FriendsEvent
    data object ClearError : FriendsEvent
}
