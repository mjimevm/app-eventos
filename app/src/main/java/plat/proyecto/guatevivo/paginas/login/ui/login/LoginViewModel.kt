package plat.proyecto.guatevivo.paginas.login.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.OnEmailChanged -> {
                _state.update { it.copy(email = event.email, errorMessage = null) }
            }
            is LoginEvent.OnPasswordChanged -> {
                _state.update { it.copy(password = event.password, errorMessage = null) }
            }
            is LoginEvent.OnTogglePasswordVisibility -> {
                _state.update { it.copy(passwordVisible = !it.passwordVisible) }
            }
            is LoginEvent.OnLoginClicked -> {
                login()
            }
            is LoginEvent.OnClearError -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    private fun login() {
        val currentState = _state.value
        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor ingresa tu correo y contraseña") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            delay(600)
            if (currentState.email.contains("@") && currentState.password.length >= 4) {
                _state.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                _state.update { it.copy(isLoading = false, errorMessage = "Correo o contraseña incorrectos. Intenta de nuevo.") }
            }
        }
    }
}
