package plat.proyecto.guatevivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import plat.proyecto.guatevivo.core.NavigationHost
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as GuateVivoApp
            val darkMode by app.preferencesRepository.darkModeFlow.collectAsStateWithLifecycle(initialValue = isSystemInDarkTheme())

            GuatevivoTheme(darkTheme = darkMode) {
                NavigationHost()
            }
        }
    }
}
