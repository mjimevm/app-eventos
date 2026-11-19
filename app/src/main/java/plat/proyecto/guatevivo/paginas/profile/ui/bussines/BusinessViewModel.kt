package plat.proyecto.guatevivo.paginas.profile.ui.bussines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.UserRepository
import plat.proyecto.guatevivo.paginas.profile.ui.user.toProximamenteItem

class BusinessViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return BusinessViewModel(app.authRepository, app.userRepository, app.eventRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(BusinessState())
    val state: StateFlow<BusinessState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        if (_state.value.isLoading || _state.value.processingEventId != null) return
        val uid = authRepository.getCurrentUserId() ?: return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(uid)
                val drafts = eventRepository.getMyDrafts()
                val createdEvents = eventRepository.getMyCreatedEvents()

                val userDrafts = drafts.filter {
                    it.status == EventStatus.DRAFT.name && (it.organizadorId == uid || it.organizadorId.isBlank())
                }.distinctBy { it.id }

                val userPublished = createdEvents.filter {
                    it.status == EventStatus.PUBLISHED.name && (it.organizadorId == uid || it.organizadorId.isBlank())
                }.distinctBy { it.id }

                _state.update {
                    it.copy(
                        empresaNombre = user?.name?.ifBlank { "Empresa Guatevivo" } ?: "Empresa Guatevivo",
                        listaCreados = userDrafts.map { dto -> dto.toProximamenteItem() },
                        listaPublicados = userPublished.map { dto -> dto.toProximamenteItem() },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al cargar el perfil comercial"
                    )
                }
            }
        }
    }

    private fun modifyDraft(eventId: String, publish: Boolean) {
        val current = _state.value
        if (current.isLoading || current.processingEventId != null || eventId.isBlank() ||
            current.listaCreados.none { it.eventId == eventId }) return
        _state.update { it.copy(processingEventId = eventId, error = null) }
        viewModelScope.launch {
            try {
                if (publish) eventRepository.publishEvent(eventId)
                else eventRepository.deleteEvent(eventId)
                _state.update { it.copy(processingEventId = null) }
                loadData()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update {
                    it.copy(
                        processingEventId = null,
                        error = e.localizedMessage ?: if (publish) "Error al publicar el borrador" else "Error al eliminar el borrador"
                    )
                }
            }
        }
    }

    fun onEvent(event: BusinessEvent) {
        when (event) {
            is BusinessEvent.OnCreateEventClicked -> {}
            is BusinessEvent.OnEventSelected -> {}
            is BusinessEvent.PublishDraft -> modifyDraft(event.eventId, publish = true)
            is BusinessEvent.DeleteDraft -> modifyDraft(event.eventId, publish = false)
            is BusinessEvent.Refresh -> loadData()
            is BusinessEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }
}
