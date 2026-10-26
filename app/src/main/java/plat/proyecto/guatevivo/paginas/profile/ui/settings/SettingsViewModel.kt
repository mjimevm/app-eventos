package plat.proyecto.guatevivo.paginas.profile.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsViewModel : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnNotificationsToggled -> _state.update { it.copy(pushNotifications = event.enabled) }
            is SettingsEvent.OnDarkModeToggled -> _state.update { it.copy(darkMode = event.enabled) }
            is SettingsEvent.OnRegionChanged -> _state.update { it.copy(region = event.region) }
        }
    }
}
