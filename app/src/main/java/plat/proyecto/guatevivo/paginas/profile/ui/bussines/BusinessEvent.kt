package plat.proyecto.guatevivo.paginas.profile.ui.bussines

sealed interface BusinessEvent {
    data object OnCreateEventClicked : BusinessEvent
    data class OnEventSelected(val eventId: String) : BusinessEvent
    data class PublishDraft(val eventId: String) : BusinessEvent
    data class DeleteDraft(val eventId: String) : BusinessEvent
    data object Refresh : BusinessEvent
    data object ClearError : BusinessEvent
}
