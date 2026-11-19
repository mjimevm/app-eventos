package plat.proyecto.guatevivo.paginas.home.ui.home

sealed interface HomeEvent {
    data class SelectCategory(val category: String) : HomeEvent
    data class UpdateQuery(val query: String) : HomeEvent
    data object Refresh : HomeEvent
    data object ResetFilters : HomeEvent
}
