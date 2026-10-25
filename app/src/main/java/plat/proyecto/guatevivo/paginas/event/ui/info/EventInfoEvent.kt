package plat.proyecto.guatevivo.paginas.event.ui.info

sealed interface EventInfoEvent {
    data object ToggleAttend : EventInfoEvent
    data object ClearMessage : EventInfoEvent
}
