package plat.proyecto.guatevivo.paginas.home.ui.search

sealed interface SearchEvent {
    data class UpdateQuery(val query: String) : SearchEvent
    data class SelectCategory(val category: String) : SearchEvent
    data class EventClicked(val eventId: Int) : SearchEvent
}
