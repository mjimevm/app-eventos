package plat.proyecto.guatevivo.paginas.home.ui.home

import androidx.annotation.DrawableRes
import plat.proyecto.guatevivo.core.EventosMockData

data class CarouselItem(
    val id: Int,
    @field:DrawableRes val imageResId: Int,
    val titulo: String,
    val ubicacion: String
)

data class ProximamenteItem(
    val title: String,
    val fecha: String,
    val dia: Int,
    val ubicacion: String,
    @field:DrawableRes val imageResId: Int,
    val cantidadAmigos: Int = 0,
    val id: Int = 0
) {
    val titulo: String get() = title
}

data class HomeState(
    val selectedCategory: String = "Todos",
    val categories: List<String> = listOf("Todos", "Conciertos", "Cultura", "Gastronomía", "Moda"),
    val destacadosItems: List<CarouselItem> = EventosMockData.destacadosList,
    val proximamenteItems: List<ProximamenteItem> = EventosMockData.proximamenteItems,
    val isLoading: Boolean = false
)
