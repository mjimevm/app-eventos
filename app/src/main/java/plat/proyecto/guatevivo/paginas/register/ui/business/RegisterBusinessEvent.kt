package plat.proyecto.guatevivo.paginas.register.ui.business

sealed interface RegisterBusinessEvent {
    data class OnEmpresaChanged(val nombre: String) : RegisterBusinessEvent
    data class OnNitChanged(val nit: String) : RegisterBusinessEvent
    data class OnCorreoChanged(val correo: String) : RegisterBusinessEvent
    data class OnPasswordChanged(val password: String) : RegisterBusinessEvent
    data object OnRegisterClicked : RegisterBusinessEvent
}
