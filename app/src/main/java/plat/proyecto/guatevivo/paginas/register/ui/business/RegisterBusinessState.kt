package plat.proyecto.guatevivo.paginas.register.ui.business

data class RegisterBusinessState(
    val empresaNombre: String = "",
    val nit: String = "",
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
