package plat.proyecto.guatevivo.paginas.event.ui.info

sealed interface EventInfoEvent {
    data object ToggleAttend : EventInfoEvent
    data object ClearMessage : EventInfoEvent
    data object Share : EventInfoEvent
    data object ShareHandled : EventInfoEvent
    data object ShareFailed : EventInfoEvent
    data object RequestDelete : EventInfoEvent
    data object CancelDelete : EventInfoEvent
    data object ConfirmDelete : EventInfoEvent
    data object DeletionHandled : EventInfoEvent
    data object Refresh : EventInfoEvent
}
