package plat.proyecto.guatevivo.paginas.home.ui.home

sealed interface HomeEvent {
    data class SelectCategory(val category: String) : HomeEvent
    data class EventClicked(val eventId: Int) : HomeEvent
}
