package plat.proyecto.guatevivo.paginas.register.ui.user

sealed interface RegisterUserEvent {
    data class OnNombreChanged(val nombre: String) : RegisterUserEvent
    data class OnCorreoChanged(val correo: String) : RegisterUserEvent
    data class OnPasswordChanged(val password: String) : RegisterUserEvent
    data object OnRegisterClicked : RegisterUserEvent
}
