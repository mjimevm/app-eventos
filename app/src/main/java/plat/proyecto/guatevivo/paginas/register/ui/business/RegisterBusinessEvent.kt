package plat.proyecto.guatevivo.paginas.register.ui.business

sealed interface RegisterBusinessEvent {
    data class OnEmpresaChanged(val nombre: String) : RegisterBusinessEvent
    data class OnCorreoChanged(val correo: String) : RegisterBusinessEvent
    data class OnTelefonoChanged(val telefono: String) : RegisterBusinessEvent
    data class OnDireccionChanged(val direccion: String) : RegisterBusinessEvent
    data class OnDescripcionChanged(val descripcion: String) : RegisterBusinessEvent
    data class OnPasswordChanged(val password: String) : RegisterBusinessEvent
    data class OnConfirmPasswordChanged(val confirmPassword: String) : RegisterBusinessEvent
    data class OnAceptoTerminosChanged(val acepto: Boolean) : RegisterBusinessEvent
    data class OnRepresentanteChanged(val rep: Boolean) : RegisterBusinessEvent
    data object OnRegisterClicked : RegisterBusinessEvent
}
