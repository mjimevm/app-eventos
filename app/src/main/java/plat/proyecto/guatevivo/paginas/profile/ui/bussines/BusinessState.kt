package plat.proyecto.guatevivo.paginas.profile.ui.bussines

data class BusinessState(
    val empresaNombre: String = "Antigua Producciones S.A.",
    val categoria: String = "Organizador de Eventos y Conciertos",
    val eventosPublicados: Int = 5,
    val seguidores: Int = 1240,
    val isLoading: Boolean = false
)
