package plat.proyecto.guatevivo.paginas.home.ui.search

import plat.proyecto.guatevivo.paginas.home.ui.searchEvents
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem

data class SearchState(
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val categories: List<String> = listOf(
        "Populares", "Conciertos", "Gastronomía",
        "Cultura", "Arte", "Danza",
        "Exhibiciones", "Religión", "Política"
    ),
    val allEvents: List<ProximamenteItem> = emptyList(),
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
) {
    val searchResults: List<ProximamenteItem>
        get() = allEvents.searchEvents(searchQuery)
}
