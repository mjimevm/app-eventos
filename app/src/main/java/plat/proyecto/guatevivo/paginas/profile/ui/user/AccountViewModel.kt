package plat.proyecto.guatevivo.paginas.profile.ui.user

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AccountViewModel : ViewModel() {
    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.OnTabSelected -> {
                // Lógica de pestañas
            }
            is AccountEvent.OnEditProfileClicked -> {
                // Lógica de editar perfil
            }
        }
    }
}
