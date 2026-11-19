package plat.proyecto.guatevivo.paginas.profile.ui.bussines

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.UserRepository

class BusinessViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return BusinessViewModel(app.authRepository, app.userRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(BusinessState())
    val state: StateFlow<BusinessState> = _state.asStateFlow()

    init {
        loadBusinessProfile()
    }

    private fun loadBusinessProfile() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getUser(uid)
                if (user != null) {
                    _state.update {
                        it.copy(
                            empresaNombre = user.name.ifBlank { "Empresa Guatevivo" },
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            } catch (_: Exception) {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onEvent(event: BusinessEvent) {
        when (event) {
            is BusinessEvent.OnCreateEventClicked -> {}
            is BusinessEvent.OnEventSelected -> {}
        }
    }
}
