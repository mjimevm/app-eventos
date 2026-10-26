package plat.proyecto.guatevivo.paginas.register.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterUserViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterUserState())
    val state: StateFlow<RegisterUserState> = _state.asStateFlow()

    fun onEvent(event: RegisterUserEvent) {
        when (event) {
            is RegisterUserEvent.OnNombreChanged -> _state.update { it.copy(nombre = event.nombre, errorMessage = null) }
            is RegisterUserEvent.OnCorreoChanged -> _state.update { it.copy(correo = event.correo, errorMessage = null) }
            is RegisterUserEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password, errorMessage = null) }
            is RegisterUserEvent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val st = _state.value
        if (st.nombre.isBlank() || st.correo.isBlank() || st.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor completa todos los campos") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            delay(600)
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
