package plat.proyecto.guatevivo.paginas.register.ui.choice

sealed interface RegisterChoiceEvent {
    data class OnRoleSelected(val role: String) : RegisterChoiceEvent
    data object OnContinueClicked : RegisterChoiceEvent
}
