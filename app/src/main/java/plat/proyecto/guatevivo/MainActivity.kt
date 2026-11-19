package plat.proyecto.guatevivo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import plat.proyecto.guatevivo.core.NavigationHost
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = application as GuateVivoApp
            val darkMode by app.preferencesRepository.darkModeFlow.collectAsStateWithLifecycle(initialValue = null)
            val useDarkTheme = darkMode ?: isSystemInDarkTheme()

            GuatevivoTheme(darkTheme = useDarkTheme) {
                val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()
                SideEffect {
                    // Reapply on theme changes, including manual overrides of the system mode.
                    val barStyle = SystemBarStyle.auto(surfaceColor, surfaceColor) { useDarkTheme }
                    enableEdgeToEdge(statusBarStyle = barStyle, navigationBarStyle = barStyle)
                }
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                    NavigationHost()
                }
            }
        }
    }
}
