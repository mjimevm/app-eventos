package plat.proyecto.guatevivo.paginas.register.ui.business

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.AuthRepository
import kotlin.coroutines.cancellation.CancellationException

class RegisterBusinessViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return RegisterBusinessViewModel(app.authRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(RegisterBusinessState())
    val state: StateFlow<RegisterBusinessState> = _state.asStateFlow()

    fun onEvent(event: RegisterBusinessEvent) {
        if (_state.value.isLoading) return
        when (event) {
            is RegisterBusinessEvent.OnEmpresaChanged -> _state.update { it.copy(empresaNombre = event.nombre, errorMessage = null) }
            is RegisterBusinessEvent.OnCorreoChanged -> _state.update { it.copy(correo = event.correo, errorMessage = null) }
            is RegisterBusinessEvent.OnTelefonoChanged -> _state.update { it.copy(telefono = event.telefono, errorMessage = null) }
            is RegisterBusinessEvent.OnDireccionChanged -> _state.update { it.copy(direccion = event.direccion, errorMessage = null) }
            is RegisterBusinessEvent.OnDescripcionChanged -> _state.update { it.copy(descripcion = event.descripcion, errorMessage = null) }
            is RegisterBusinessEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password, errorMessage = null) }
            is RegisterBusinessEvent.OnConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null) }
            is RegisterBusinessEvent.OnAceptoTerminosChanged -> _state.update { it.copy(aceptoTerminos = event.acepto, errorMessage = null) }
            is RegisterBusinessEvent.OnRepresentanteChanged -> _state.update { it.copy(esRepresentante = event.rep, errorMessage = null) }
            is RegisterBusinessEvent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val st = _state.value
        if (st.isLoading) return
        if (st.empresaNombre.isBlank() || st.correo.isBlank() || st.password.isBlank() || st.confirmPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor completa todos los campos obligatorios de la empresa") }
            return
        }
        if (st.password != st.confirmPassword) {
            _state.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }
        if (st.password.length < 6) {
            _state.update { it.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres") }
            return
        }
        if (!st.aceptoTerminos || !st.esRepresentante) {
            _state.update { it.copy(errorMessage = "Debes aceptar los términos y confirmar que representas a la empresa") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.register(
                    email = st.correo,
                    password = st.password,
                    userDto = UserDto(
                        name = st.empresaNombre.trim(),
                        email = st.correo.trim().lowercase(),
                        phone = st.telefono.trim(),
                        address = st.direccion.trim(),
                        description = st.descripcion.trim(),
                        accountType = AccountType.BUSINESS.name
                    )
                )
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo registrar la cuenta empresarial. Intenta nuevamente."
                    )
                }
            }
        }
    }
}
