package plat.proyecto.guatevivo.paginas.login.ui.login

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
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.UserRepository
import kotlin.coroutines.cancellation.CancellationException

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return LoginViewModel(app.authRepository, app.userRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        if (_state.value.isLoading && event !is LoginEvent.OnClearError) return
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                _state.update { it.copy(email = event.email, errorMessage = null, successMessage = null) }
            }
            is LoginEvent.OnPasswordChanged -> {
                _state.update { it.copy(password = event.password, errorMessage = null, successMessage = null) }
            }
            is LoginEvent.OnTogglePasswordVisibility -> {
                _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            }
            is LoginEvent.OnLoginClicked -> {
                login()
            }
            is LoginEvent.OnForgotPasswordClicked -> {
                resetPassword()
            }
            is LoginEvent.OnClearError -> {
                _state.update { it.copy(errorMessage = null, successMessage = null) }
            }
        }
    }

    private fun login() {
        val st = _state.value
        if (st.isLoading) return
        if (st.email.isBlank() || st.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor ingresa tu correo y contraseña") }
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            try {
                val uid = authRepository.login(st.email, st.password)
                val userDto = userRepository.getUser(uid)
                    ?: throw IllegalStateException("Perfil de usuario no encontrado en la base de datos")
                val accountType = userDto.accountType
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        accountType = accountType
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Credenciales incorrectas o error de conexión. Verifica tus datos."
                    )
                }
            }
        }
    }

    private fun resetPassword() {
        val st = _state.value
        if (st.email.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor ingresa tu correo para recuperar la contraseña") }
            return
        }
        _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
        viewModelScope.launch {
            try {
                authRepository.resetPassword(st.email)
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Se ha enviado un correo para restablecer tu contraseña."
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo enviar el correo de recuperación. Verifica tu dirección."
                    )
                }
            }
        }
    }
}
