package plat.proyecto.guatevivo.paginas.home.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is HomeEvent.EventClicked -> {
                // Handle event click
            }
        }
    }
}
