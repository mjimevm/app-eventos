package plat.proyecto.guatevivo.paginas.register.ui.business

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterBusinessViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterBusinessState())
    val state: StateFlow<RegisterBusinessState> = _state.asStateFlow()

    fun onEvent(event: RegisterBusinessEvent) {
        when (event) {
            is RegisterBusinessEvent.OnEmpresaChanged -> _state.update { it.copy(empresaNombre = event.nombre, errorMessage = null) }
            is RegisterBusinessEvent.OnNitChanged -> _state.update { it.copy(nit = event.nit, errorMessage = null) }
            is RegisterBusinessEvent.OnCorreoChanged -> _state.update { it.copy(correo = event.correo, errorMessage = null) }
            is RegisterBusinessEvent.OnPasswordChanged -> _state.update { it.copy(password = event.password, errorMessage = null) }
            is RegisterBusinessEvent.OnRegisterClicked -> register()
        }
    }

    private fun register() {
        val st = _state.value
        if (st.empresaNombre.isBlank() || st.nit.isBlank() || st.correo.isBlank() || st.password.isBlank()) {
            _state.update { it.copy(errorMessage = "Por favor completa todos los campos de la empresa") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            delay(600)
            _state.update { it.copy(isLoading = false, isSuccess = true) }
        }
    }
}
