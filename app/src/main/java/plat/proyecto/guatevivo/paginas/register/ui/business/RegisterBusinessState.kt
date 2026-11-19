package plat.proyecto.guatevivo.paginas.register.ui.business

data class RegisterBusinessState(
    val empresaNombre: String = "",
    val correo: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val descripcion: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val aceptoTerminos: Boolean = false,
    val esRepresentante: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val partialUid: String? = null
)
