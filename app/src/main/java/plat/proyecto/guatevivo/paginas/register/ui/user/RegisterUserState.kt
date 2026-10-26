package plat.proyecto.guatevivo.paginas.register.ui.user

data class RegisterUserState(
    val nombre: String = "",
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)
