package plat.proyecto.guatevivo.paginas.profile.ui.user

sealed interface AccountEvent {
    data class OnTabSelected(val tabIndex: Int) : AccountEvent
    data object OnEditProfileClicked : AccountEvent
}
