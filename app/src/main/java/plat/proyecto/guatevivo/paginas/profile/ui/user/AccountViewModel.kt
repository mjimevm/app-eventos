package plat.proyecto.guatevivo.paginas.profile.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.UserRepository
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem

class AccountViewModel(
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
                return AccountViewModel(app.authRepository, app.userRepository, app.eventRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val user = userRepository.getUser(uid)
                val drafts = eventRepository.getMyDrafts()
                val createdEvents = eventRepository.getMyCreatedEvents()

                val userDrafts = drafts.filter {
                    it.status == EventStatus.DRAFT.name && it.organizadorId == uid
                }.distinctBy { it.id }

                val userPublished = createdEvents.filter {
                    it.status == EventStatus.PUBLISHED.name && it.organizadorId == uid
                }.distinctBy { it.id }

                _state.update {
                    it.copy(
                        usuario = user?.name?.ifBlank { "Usuario Guatevivo" } ?: "Usuario Guatevivo",
                        correo = user?.email?.ifBlank { authRepository.getCurrentUserEmail() ?: "" }
                            ?: (authRepository.getCurrentUserEmail() ?: ""),
                        listaCreados = userDrafts.map { dto -> dto.toProximamenteItem() },
                        listaCompartidos = userPublished.map { dto -> dto.toProximamenteItem() },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.localizedMessage ?: "Error al cargar el perfil"
                    )
                }
            }
        }
    }

    fun onEvent(event: AccountEvent) {
        when (event) {
            is AccountEvent.OnTabSelected -> {}
            is AccountEvent.OnEditProfileClicked -> {}
            is AccountEvent.Refresh -> loadData()
            is AccountEvent.ClearError -> _state.update { it.copy(error = null) }
        }
    }
}

fun EventDto.toProximamenteItem(): ProximamenteItem {
    val date = try {
        LocalDate.parse(fecha, DateTimeFormatter.ofPattern("dd/MM/uuuu"))
    } catch (_: DateTimeParseException) { null }
    val diaParsed = date?.dayOfMonth ?: (fecha.filter { it.isDigit() }.take(2).toIntOrNull() ?: 1)
    val mesParsed = date?.format(DateTimeFormatter.ofPattern("MMM", Locale.forLanguageTag("es-GT")))
        ?.removeSuffix(".")?.uppercase() ?: if (fecha.length >= 3) fecha.take(3).uppercase() else "NOV"
    return ProximamenteItem(
        title = nombreEvento.ifBlank { "Sin título" },
        fecha = mesParsed,
        dia = diaParsed,
        ubicacion = ubicacion.ifBlank { "Guatemala" },
        imageResId = R.drawable.festival,
        cantidadAmigos = 0,
        id = id.hashCode(),
        imageUrl = imagenUrl,
        eventId = id
    )
}
