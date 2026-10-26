package plat.proyecto.guatevivo.paginas.profile.ui.friends

sealed interface FriendsEvent {
    data class OnSearchQueryChanged(val query: String) : FriendsEvent
    data class OnAddFriendClicked(val friendId: Int) : FriendsEvent
}
