package plat.proyecto.guatevivo.paginas.register.ui.user

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

class RegisterUserViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return RegisterUserViewModel(app.authRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(RegisterUserState())
    val state: StateFlow<RegisterUserState> = _state.asStateFlow()

    fun onEvent(event: RegisterUserEvent) {
        if (_state.value.isLoading) return
        when (event) {
            is RegisterUserEvent.OnNombreChanged -> _state.update { it.copy(nombre = event.nombre, errorMessage = null) }
            is RegisterUserEvent.OnApellidoChanged -> _state.update { it.copy(apellido = event.apellido, errorMessage = null) }
            is RegisterUserEvent.OnCorreoChanged -> _state.update { it.copy(correo = event.correo, errorMessage = null) }
            is RegisterUserEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password, errorMessage = null) }
            is RegisterUserEvent.OnConfirmPasswordChanged -> _state.update { it.copy(confirmPassword = event.confirmPassword, errorMessage = null) }
            is RegisterUserEvent.OnAceptoTerminosChanged -> _state.update { it.copy(aceptoTerminos = event.acepto, errorMessage = null) }
            is RegisterUserEvent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val st = _state.value
        if (st.isLoading) return
        if (st.nombre.isBlank() || st.apellido.isBlank() || st.correo.isBlank() || st.password.isBlank() || st.confirmPassword.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor completa todos los campos obligatorios") }
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
        if (!st.aceptoTerminos) {
            _state.update { it.copy(errorMessage = "Debes aceptar los términos y condiciones") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val fullName = "${st.nombre.trim()} ${st.apellido.trim()}"
                authRepository.register(
                    email = st.correo,
                    password = st.password,
                    userDto = UserDto(
                        name = fullName,
                        email = st.correo.trim().lowercase(),
                        accountType = AccountType.USER.name
                    )
                )
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo completar el registro. Verifica tu correo o intenta más tarde."
                    )
                }
            }
        }
    }
}
