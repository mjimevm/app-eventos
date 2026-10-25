package plat.proyecto.guatevivo.paginas.home.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SearchViewModel : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()

    fun onEvent(event: SearchEvent) {
        when (event) {
            is SearchEvent.UpdateQuery -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is SearchEvent.SelectCategory -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }
            is SearchEvent.EventClicked -> {

            }
        }
    }
}
