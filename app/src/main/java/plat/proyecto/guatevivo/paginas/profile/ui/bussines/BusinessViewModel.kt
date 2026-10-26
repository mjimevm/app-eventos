package plat.proyecto.guatevivo.paginas.profile.ui.bussines

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BusinessViewModel : ViewModel() {
    private val _state = MutableStateFlow(BusinessState())
    val state: StateFlow<BusinessState> = _state.asStateFlow()

    fun onEvent(event: BusinessEvent) {
        when (event) {
            is BusinessEvent.OnCreateEventClicked -> {}
            is BusinessEvent.OnEventSelected -> {}
        }
    }
}
