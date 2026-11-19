package plat.proyecto.guatevivo.paginas.home.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.paginas.home.ui.catalogErrorMessage
import plat.proyecto.guatevivo.paginas.home.ui.searchEvents
import plat.proyecto.guatevivo.paginas.home.ui.upcomingPublicEvents

class HomeViewModel(
    private val eventRepository: EventRepository,
    private val clock: Clock = Clock.system(ZoneId.of("America/Guatemala"))
) : ViewModel() {
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as GuateVivoApp
                return HomeViewModel(app.eventRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    private var allEvents = emptyList<ProximamenteItem>()
    private var hasResumed = false

    init { refresh() }

    fun onResume() {
        if (hasResumed) refresh()
        hasResumed = true
    }

    fun refresh() {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                val result = eventRepository.getPublicEventsResult()
                allEvents = upcomingPublicEvents(result.events, LocalDateTime.now(clock))
                _state.update { it.copy(isLoading = false, isFromCache = result.isFromCache) }
                updateResults()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                allEvents = emptyList()
                _state.update { it.copy(isLoading = false, isFromCache = false, error = catalogErrorMessage(e)) }
                updateResults()
            }
        }
    }

    private fun updateResults() {
        _state.update { current ->
            val results = allEvents.searchEvents(current.searchQuery)
            current.copy(
                proximamenteItems = results,
                // No popularity/featured field exists; highlight the next events chronologically.
                destacadosItems = results.take(5).map {
                    CarouselItem(
                        id = 0, imageResId = 0, titulo = it.title, ubicacion = it.ubicacion,
                        eventId = it.eventId, imageUrl = it.imageUrl, fechaHora = "${it.fullDate} ${it.hora}"
                    )
                }
            )
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectCategory -> {
                // Other categories cannot be applied until EventDto has a persisted category.
                if (event.category == "Todos") _state.update { it.copy(selectedCategory = "Todos") }
            }
            is HomeEvent.UpdateQuery -> {
                _state.update { it.copy(searchQuery = event.query) }
                updateResults()
            }
            HomeEvent.Refresh -> refresh()
            HomeEvent.ResetFilters -> {
                _state.update { it.copy(searchQuery = "", selectedCategory = "Todos") }
                updateResults()
            }
        }
    }
}
