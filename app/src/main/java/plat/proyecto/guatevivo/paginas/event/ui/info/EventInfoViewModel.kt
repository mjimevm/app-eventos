package plat.proyecto.guatevivo.paginas.event.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.CancellationException
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.repository.EventRepository
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import plat.proyecto.guatevivo.core.EventosMockData
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility

class EventInfoViewModel(
    private val destination: EventInfoDestination,
    private val eventRepository: EventRepository,
    private val currentUserId: () -> String? = { null }
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as GuateVivoApp
                return EventInfoViewModel(
                    extras.createSavedStateHandle().toRoute<EventInfoDestination>(),
                    app.eventRepository, app.authRepository::getCurrentUserId
                ) as T
            }
        }
    }

    private val _state = MutableStateFlow(
        destination.mockEventId?.let { EventosMockData.getEventInfoState(it) }
            ?: EventInfoState(
                titulo = "", miniDescripcion = "", dia = "", mes = "", ubicacion = "",
                precio = "", descripcionLarga = "", amigosAsistiran = emptyList(),
                imageResId = 0, canAttend = false, eventId = destination.eventId
            )
    )
    val state: StateFlow<EventInfoState> = _state.asStateFlow()
    private var loadedEvent: EventDto? = null
    private var hasResumed = false
    private val isBusy: Boolean get() = _state.value.let { it.isLoading || it.isDeleting || it.isSharing }

    init { loadEvent() }

    fun onResume() {
        if (hasResumed) loadEvent()
        hasResumed = true
    }

    private fun loadEvent() {
        if (destination.mockEventId == null && !isBusy && !_state.value.isDeleted) {
            _state.update { it.copy(isLoading = true, error = null) }
            viewModelScope.launch {
                try {
                    val event = eventRepository.getEventById(destination.eventId)
                        ?: throw IllegalArgumentException("El evento no existe o ya no está disponible")
                    loadedEvent = event
                    val uid = currentUserId()
                    val date = try {
                        LocalDate.parse(event.fecha, DateTimeFormatter.ofPattern("dd/MM/uuuu"))
                    } catch (_: DateTimeParseException) { null }
                    _state.update {
                        it.copy(
                            titulo = event.nombreEvento,
                            miniDescripcion = listOf(event.fecha, event.hora).filter { value -> value.isNotBlank() }.joinToString(" · "),
                            dia = date?.dayOfMonth?.toString() ?: event.fecha,
                            mes = date?.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("es-GT"))) ?: "",
                            ubicacion = event.ubicacion,
                            descripcionLarga = event.descripcion,
                            imageUrl = event.imagenUrl,
                            eventId = event.id,
                            canManage = !uid.isNullOrBlank() && uid == event.organizadorId,
                            canShare = event.status == EventStatus.PUBLISHED.name && event.visibility == EventVisibility.PUBLIC.name,
                            isLoading = false
                        )
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    loadedEvent = null
                    _state.update {
                        it.copy(isLoading = false, canManage = false, canShare = false,
                            titulo = "", miniDescripcion = "", descripcionLarga = "", imageUrl = "",
                            dia = "", mes = "", ubicacion = "", shareText = null, showDeleteConfirmation = false,
                            error = e.localizedMessage ?: "Error al cargar el evento")
                    }
                }
            }
        }
    }

    private fun shareEvent() {
        if (isBusy || !_state.value.canShare || _state.value.shareText != null) return
        _state.update { it.copy(isSharing = true, error = null) }
        viewModelScope.launch {
            try {
                // Never use an old/offline visibility decision to export a private event.
                val event = eventRepository.getEventForSharing(destination.eventId)
                    ?: error("El evento ya no está disponible para compartir")
                val text = event.publicShareText() ?: error("Este evento no se puede compartir. Revisa su fecha y hora.")
                _state.update { it.copy(isSharing = false, shareText = text) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(isSharing = false, shareText = null, error = e.localizedMessage ?: "Error al compartir el evento") }
            }
        }
    }

    private fun deleteEvent() {
        val event = loadedEvent ?: return
        val uid = currentUserId()
        if (isBusy || !_state.value.showDeleteConfirmation || !_state.value.canManage ||
            uid.isNullOrBlank() || uid != event.organizadorId) return
        _state.update { it.copy(isDeleting = true, error = null) }
        viewModelScope.launch {
            try {
                eventRepository.deleteEvent(event.id)
                loadedEvent = null
                _state.update {
                    it.copy(isDeleting = false, showDeleteConfirmation = false, isDeleted = true, canManage = false, canShare = false)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update { it.copy(isDeleting = false, error = e.localizedMessage ?: "Error al eliminar el evento") }
            }
        }
    }

    fun onEvent(event: EventInfoEvent) {
        when (event) {
            is EventInfoEvent.ToggleAttend -> {
                if (!_state.value.canAttend || _state.value.isLoading) return
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
                _state.update { it.copy(successMessage = null, error = null) }
            }
            EventInfoEvent.Share -> shareEvent()
            EventInfoEvent.ShareHandled -> _state.update { it.copy(shareText = null) }
            EventInfoEvent.ShareFailed -> _state.update { it.copy(shareText = null, error = "No se pudo abrir el menú para compartir") }
            EventInfoEvent.RequestDelete -> if (!isBusy && _state.value.canManage) {
                _state.update { it.copy(showDeleteConfirmation = true, error = null) }
            }
            EventInfoEvent.CancelDelete -> if (!_state.value.isDeleting) _state.update { it.copy(showDeleteConfirmation = false) }
            EventInfoEvent.ConfirmDelete -> deleteEvent()
            EventInfoEvent.DeletionHandled -> _state.update { it.copy(isDeleted = false) }
            EventInfoEvent.Refresh -> loadEvent()
        }
    }
}
