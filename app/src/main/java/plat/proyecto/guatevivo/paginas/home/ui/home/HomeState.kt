package plat.proyecto.guatevivo.paginas.home.ui.home

import androidx.annotation.DrawableRes

data class CarouselItem(
    val id: Int,
    @field:DrawableRes val imageResId: Int,
    val titulo: String,
    val ubicacion: String,
    val eventId: String = "",
    val imageUrl: String = "",
    val fechaHora: String = ""
)

data class ProximamenteItem(
    val title: String,
    val fecha: String,
    val dia: Int,
    val ubicacion: String,
    @field:DrawableRes val imageResId: Int,
    val cantidadAmigos: Int = 0,
    val id: Int = 0,
    val imageUrl: String = "",
    val eventId: String = "",
    val fullDate: String = "",
    val hora: String = "",
    val descripcion: String = ""
) {
    val titulo: String get() = title
}

data class HomeState(
    val selectedCategory: String = "Todos",
    val categories: List<String> = listOf("Todos", "Conciertos", "Cultura", "Gastronomía", "Moda"),
    val destacadosItems: List<CarouselItem> = emptyList(),
    val proximamenteItems: List<ProximamenteItem> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val error: String? = null
)
