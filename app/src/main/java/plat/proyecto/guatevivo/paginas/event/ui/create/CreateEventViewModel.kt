package plat.proyecto.guatevivo.paginas.event.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.core.repository.EventRepository
import com.google.firebase.auth.FirebaseAuth
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import kotlinx.coroutines.CancellationException

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val eventId: String? = null,
    private val currentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid }
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState(eventId = eventId, canSave = eventId == null))
    val state: StateFlow<CreateEventState> = _state.asStateFlow()
    private var originalEvent: EventDto? = null

    init { if (eventId != null) loadEvent() }

    private fun loadEvent() {
        if (eventId == null || _state.value.isLoading) return
        _state.update { it.copy(isLoading = true, canSave = false, error = null) }
        viewModelScope.launch {
            try {
                val event = eventRepository.getEventById(eventId) ?: error("El evento no existe o fue eliminado")
                val uid = currentUserId()
                require(!uid.isNullOrBlank() && event.organizadorId == uid) {
                    "No puedes editar eventos de otros usuarios"
                }
                originalEvent = event
                _state.update {
                    it.copy(
                        nombreEvento = event.nombreEvento, ubicacion = event.ubicacion,
                        fecha = event.fecha, hora = event.hora, descripcion = event.descripcion,
                        incluirConfirmacion = event.incluirConfirmacion, imageUri = event.imagenUrl.ifBlank { null },
                        isLoading = false, canSave = true
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, canSave = false, error = e.localizedMessage ?: "Error al cargar el evento") }
            }
        }
    }

    fun isValidDate(dateStr: String): Boolean {
        if (dateStr.isBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                .withResolverStyle(ResolverStyle.STRICT)
            LocalDate.parse(dateStr.trim(), formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isValidTime(timeStr: String): Boolean {
        if (timeStr.isBlank()) return false
        return try {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
                .withResolverStyle(ResolverStyle.STRICT)
            LocalTime.parse(timeStr.trim(), formatter)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun onEvent(event: CreateEventEvent) {
        if (_state.value.isLoading) return
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
            is CreateEventEvent.ClearSuccess -> {
                if (_state.value.isEditing) {
                    _state.update { it.copy(isSuccess = false) }
                    return
                }
                _state.update {
                    it.copy(
                        isSuccess = false,
                        nombreEvento = "",
                        ubicacion = "",
                        fecha = "",
                        hora = "",
                        descripcion = "",
                        incluirConfirmacion = true,
                        imageUri = null,
                        error = null
                    )
                }
            }
            CreateEventEvent.RetryLoad -> loadEvent()
        }
    }
    private fun createEvent() {
        val currentState = _state.value

        // Evitar solicitudes duplicadas
        if (currentState.isLoading || currentState.isSuccess || !currentState.canSave) return

        // Validar formulario
        if (currentState.nombreEvento.isBlank()) {
            _state.update {
                it.copy(error = "El nombre del evento es requerido")
            }
            return
        }

        if (currentState.fecha.isBlank()) {
            _state.update {
                it.copy(error = "La fecha es requerida (dd/MM/yyyy)")
            }
            return
        }

        if (!isValidDate(currentState.fecha)) {
            _state.update {
                it.copy(error = "Fecha inválida. Usa el formato dd/MM/yyyy (ej. 21/02/2027)")
            }
            return
        }

        if (currentState.hora.isBlank()) {
            _state.update {
                it.copy(error = "La hora es requerida (HH:mm)")
            }
            return
        }

        if (!isValidTime(currentState.hora)) {
            _state.update {
                it.copy(error = "Hora inválida. Usa el formato HH:mm en 24 horas (ej. 14:30)")
            }
            return
        }

        // Obtener usuario autenticado
        val userId = currentUserId()

        if (userId == null) {
            _state.update {
                it.copy(error = "Debes iniciar sesión para guardar un evento")
            }
            return
        }

        val original = originalEvent
        if (eventId != null && (original == null || original.organizadorId != userId)) {
            _state.update { it.copy(error = "No puedes editar este evento", canSave = false) }
            return
        }

        _state.update { it.copy(isLoading = true, isSuccess = false, error = null) }

        viewModelScope.launch {
            try {
                // Construir el evento con los datos del formulario
                val eventDto = (original ?: EventDto(
                    organizadorId = userId,
                    status = EventStatus.DRAFT.name,
                    visibility = EventVisibility.PRIVATE.name,
                    createdAt = System.currentTimeMillis()
                )).copy(
                    nombreEvento = currentState.nombreEvento.trim(),
                    ubicacion = currentState.ubicacion.trim(),
                    fecha = currentState.fecha.trim(),
                    hora = currentState.hora.trim(),
                    descripcion = currentState.descripcion.trim(),
                    incluirConfirmacion = currentState.incluirConfirmacion
                )

                if (original == null) {
                    eventRepository.createDraft(eventDto, currentState.imageUri)
                } else {
                    val imageUri = currentState.imageUri.orEmpty()
                    val imageUrl = if (imageUri.startsWith("content://") || imageUri.startsWith("file://")) {
                        eventRepository.uploadEventImage(original.id, imageUri)
                    } else imageUri
                    val updated = eventDto.copy(imagenUrl = imageUrl)
                    if (original.status == EventStatus.DRAFT.name) eventRepository.updateDraft(updated)
                    else eventRepository.updateEvent(updated)
                    originalEvent = updated
                    _state.update { it.copy(imageUri = imageUrl.ifBlank { null }) }
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isSuccess = false,
                        error = e.localizedMessage
                            ?: "Error al guardar el evento"
                    )
                }
            }
        }
    }
}
