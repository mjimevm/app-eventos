package plat.proyecto.guatevivo.paginas.home.ui.search

import plat.proyecto.guatevivo.core.EventosDb
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem

data class SearchState(
    val searchQuery: String = "",
    val selectedCategory: String = "Populares",
    val categories: List<String> = listOf(
        "Populares", "Conciertos", "Gastronomía",
        "Cultura", "Arte", "Danza",
        "Exhibiciones", "Religión", "Política"
    ),
    val allEvents: List<ProximamenteItem> = EventosDb.proximamenteItems,
    val isLoading: Boolean = false
) {
    val searchResults: List<ProximamenteItem>
        get() = if (searchQuery.isBlank()) {
            allEvents
        } else {
            allEvents.filter {
                it.titulo.contains(searchQuery, ignoreCase = true) ||
                        it.ubicacion.contains(searchQuery, ignoreCase = true)
            }
        }
}
