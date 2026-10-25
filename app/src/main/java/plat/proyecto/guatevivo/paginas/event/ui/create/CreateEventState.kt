package plat.proyecto.guatevivo.paginas.event.ui.create

data class CreateEventState(
    val nombreEvento: String = "",
    val ubicacion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val descripcion: String = "",
    val incluirConfirmacion: Boolean = true,
    val imageUri: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)
