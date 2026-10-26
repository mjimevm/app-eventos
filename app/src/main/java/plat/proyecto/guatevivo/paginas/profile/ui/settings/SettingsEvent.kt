package plat.proyecto.guatevivo.paginas.profile.ui.settings

sealed interface SettingsEvent {
    data class OnNotificationsToggled(val enabled: Boolean) : SettingsEvent
    data class OnDarkModeToggled(val enabled: Boolean) : SettingsEvent
    data class OnRegionChanged(val region: String) : SettingsEvent
}
