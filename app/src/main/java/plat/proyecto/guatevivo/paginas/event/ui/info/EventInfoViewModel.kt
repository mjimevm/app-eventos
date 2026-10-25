package plat.proyecto.guatevivo.paginas.event.ui.info

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.core.EventosDb

class EventInfoViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val destination = savedStateHandle.toRoute<EventInfoDestination>()

    private val _state = MutableStateFlow(
        EventosDb.getEventInfoState(destination.eventId)
    )
    val state: StateFlow<EventInfoState> = _state.asStateFlow()

    fun onEvent(event: EventInfoEvent) {
        when (event) {
            is EventInfoEvent.ToggleAttend -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    delay(500)
                    _state.update { currentState ->
                        val newAttending = !currentState.isAttending
                        currentState.copy(
                            isLoading = false,
                            isAttending = newAttending,
                            successMessage = if (newAttending) "¡Te has registrado al evento exitosamente!" else "Has cancelado tu asistencia al evento."
                        )
                    }
                }
            }
            is EventInfoEvent.ClearMessage -> {
                _state.update { it.copy(successMessage = null) }
            }
        }
    }
}
