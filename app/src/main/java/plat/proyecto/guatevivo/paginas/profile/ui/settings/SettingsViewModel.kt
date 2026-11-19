package plat.proyecto.guatevivo.paginas.profile.ui.settings

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.datastore.PreferencesRepository
import plat.proyecto.guatevivo.core.repository.AuthRepository

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val app = application as GuateVivoApp
                return SettingsViewModel(app.authRepository, app.preferencesRepository) as T
            }
        }
    }

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepository.darkModeFlow.collectLatest { isDark ->
                _state.update { it.copy(darkMode = isDark) }
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnNotificationsToggled -> {
                _state.update { it.copy(pushNotifications = event.enabled) }
            }
            is SettingsEvent.OnDarkModeToggled -> {
                viewModelScope.launch {
                    preferencesRepository.setDarkMode(event.enabled)
                }
            }
            is SettingsEvent.OnRegionChanged -> {
                _state.update { it.copy(region = event.region) }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }
}
