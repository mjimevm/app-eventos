package plat.proyecto.guatevivo.paginas.profile.ui.bussines

sealed interface BusinessEvent {
    data object OnCreateEventClicked : BusinessEvent
    data class OnEventSelected(val eventId: String) : BusinessEvent
}
