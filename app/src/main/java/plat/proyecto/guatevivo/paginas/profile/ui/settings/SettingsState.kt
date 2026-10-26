package plat.proyecto.guatevivo.paginas.profile.ui.settings

data class SettingsState(
    val pushNotifications: Boolean = true,
    val darkMode: Boolean = false,
    val region: String = "Guatemala"
)
