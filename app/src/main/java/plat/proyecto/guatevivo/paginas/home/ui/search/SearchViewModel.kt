package plat.proyecto.guatevivo.paginas.home.ui.search

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
import plat.proyecto.guatevivo.paginas.home.ui.upcomingPublicEvents

class SearchViewModel(
    private val eventRepository: EventRepository,
    private val clock: Clock = Clock.system(ZoneId.of("America/Guatemala"))
) : ViewModel() {
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as GuateVivoApp
                return SearchViewModel(app.eventRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()
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
                val events = upcomingPublicEvents(result.events, LocalDateTime.now(clock))
                _state.update { it.copy(allEvents = events, isLoading = false, isFromCache = result.isFromCache) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _state.update {
                    it.copy(allEvents = emptyList(), isLoading = false, isFromCache = false, error = catalogErrorMessage(e))
                }
            }
        }
    }

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.UpdateQuery -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is SearchEvent.SelectCategory -> {
                // Categories and popularity have no backing field in the current model.
            }
            SearchEvent.Refresh -> refresh()
            SearchEvent.ResetFilters -> _state.update { it.copy(searchQuery = "", selectedCategory = "") }
        }
    }
}
