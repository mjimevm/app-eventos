package plat.proyecto.guatevivo.paginas.event.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateEventViewModel : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state.asStateFlow()

    fun onEvent(event: CreateEventEvent) {
        when (event) {
            is CreateEventEvent.UpdateNombreEvento -> {
                _state.update { it.copy(nombreEvento = event.nombre) }
            }
            is CreateEventEvent.UpdateUbicacion -> {
                _state.update { it.copy(ubicacion = event.ubicacion) }
            }
            is CreateEventEvent.UpdateFecha -> {
                _state.update { it.copy(fecha = event.fecha) }
            }
            is CreateEventEvent.UpdateHora -> {
                _state.update { it.copy(hora = event.hora) }
            }
            is CreateEventEvent.UpdateDescripcion -> {
                _state.update { it.copy(descripcion = event.descripcion) }
            }
            is CreateEventEvent.UpdateIncluirConfirmacion -> {
                _state.update { it.copy(incluirConfirmacion = event.incluir) }
            }
            is CreateEventEvent.UpdateImageUri -> {
                _state.update { it.copy(imageUri = event.uri) }
            }
            is CreateEventEvent.CreateEvent -> {
                createEvent()
            }
            is CreateEventEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun createEvent() {
        val currentState = _state.value
        if (currentState.nombreEvento.isBlank()) {
            _state.update { it.copy(error = "El nombre del evento es requerido") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                kotlinx.coroutines.delay(2000)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Error al crear el evento") }
            }
        }
    }
}
