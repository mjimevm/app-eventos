package plat.proyecto.guatevivo.paginas.register.ui.business

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
import plat.proyecto.guatevivo.core.repository.RegistrationException
import java.util.Locale
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
    private var pendingProfile: UserDto? = null

    fun onEvent(event: RegisterBusinessEvent) {
        if (_state.value.isLoading) return
        if (_state.value.partialUid != null && event != RegisterBusinessEvent.OnRegisterClicked) return
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
        if (st.isSuccess) return
        if (st.partialUid != null) {
            submitProfile(st.partialUid, checkNotNull(pendingProfile))
            return
        }
        if (st.empresaNombre.isBlank() || st.correo.isBlank() || st.password.isBlank() || st.confirmPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor completa todos los campos obligatorios de la empresa") }
            return
        }
        if (st.empresaNombre.trim().length > 120) {
            _state.update { it.copy(errorMessage = "El nombre de la empresa admite hasta 120 caracteres") }
            return
        }
        if (!Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(st.correo.trim())) {
            _state.update { it.copy(errorMessage = "Ingresa un correo empresarial válido") }
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

        submitProfile(null, UserDto(
            name = st.empresaNombre.trim(),
            email = st.correo.trim().lowercase(Locale.ROOT),
            phone = st.telefono.trim(),
            address = st.direccion.trim(),
            description = st.descripcion.trim(),
            accountType = AccountType.BUSINESS.name
        ))
    }

    private fun submitProfile(partialUid: String?, profile: UserDto) {
        val password = _state.value.password
        pendingProfile = profile
        _state.update { it.copy(isLoading = true, errorMessage = null, isSuccess = false) }
        viewModelScope.launch {
            try {
                if (partialUid == null) {
                    authRepository.register(profile.email, password, profile)
                } else {
                    authRepository.retryRegistrationProfile(partialUid, profile)
                }
                _state.update { it.copy(isLoading = false, isSuccess = true, partialUid = null,
                    password = "", confirmPassword = "") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        partialUid = (e as? RegistrationException)?.partialUid ?: partialUid,
                        errorMessage = e.localizedMessage ?: "No se pudo registrar la cuenta empresarial (${e.javaClass.simpleName})"
                    )
                }
            }
        }
    }
}
