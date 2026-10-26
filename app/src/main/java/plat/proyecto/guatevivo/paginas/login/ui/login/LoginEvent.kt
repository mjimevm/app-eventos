package plat.proyecto.guatevivo.paginas.login.ui.login

sealed interface LoginEvent {
    data class OnEmailChanged(val email: String) : LoginEvent
    data class OnPasswordChanged(val password: String) : LoginEvent
    data object OnTogglePasswordVisibility : LoginEvent
    data object OnLoginClicked : LoginEvent
    data object OnClearError : LoginEvent
}
