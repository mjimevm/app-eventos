package plat.proyecto.guatevivo.paginas.profile.ui.friends

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FriendsViewModel : ViewModel() {
    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state.asStateFlow()

    fun onEvent(event: FriendsEvent) {
        when (event) {
            is FriendsEvent.OnSearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
            }
            is FriendsEvent.OnAddFriendClicked -> {
                // Lógica de agregar amigo
            }
        }
    }
}
