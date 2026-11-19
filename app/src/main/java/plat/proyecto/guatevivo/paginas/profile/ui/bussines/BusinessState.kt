package plat.proyecto.guatevivo.paginas.profile.ui.bussines

import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem

data class BusinessState(
    val empresaNombre: String = "",
    val categoria: String = "",
    val listaCreados: List<ProximamenteItem> = emptyList(),
    val listaPublicados: List<ProximamenteItem> = emptyList(),
    val isLoading: Boolean = false,
    val processingEventId: String? = null,
    val error: String? = null
)
