package plat.proyecto.guatevivo.paginas.register.ui.choice

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterChoiceViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegisterChoiceState())
    val state: StateFlow<RegisterChoiceState> = _state.asStateFlow()

    fun onEvent(event: RegisterChoiceEvent) {
        when (event) {
            is RegisterChoiceEvent.OnRoleSelected -> _state.update { it.copy(selectedRole = event.role) }
            is RegisterChoiceEvent.OnContinueClicked -> {}
        }
    }
}
