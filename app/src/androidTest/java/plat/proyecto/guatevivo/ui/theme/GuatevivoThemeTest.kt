package plat.proyecto.guatevivo.ui.theme

import android.content.res.Configuration
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class GuatevivoThemeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun defaultTheme_reactsToSystemChangesAndPreservesBrandPalettes() {
        val configuration = mutableStateOf(Configuration().apply {
            uiMode = Configuration.UI_MODE_NIGHT_NO
        })
        var colors: ColorScheme? = null
        compose.setContent {
            CompositionLocalProvider(LocalConfiguration provides configuration.value) {
                GuatevivoTheme {
                    val current = MaterialTheme.colorScheme
                    SideEffect { colors = current }
                }
            }
        }
        compose.runOnIdle {
            assertEquals(primaryLight, colors?.primary)
            assertEquals(surfaceLight, colors?.surface)
            assertEquals(onSurfaceLight, colors?.onSurface)
            configuration.value = Configuration(configuration.value).apply {
                uiMode = Configuration.UI_MODE_NIGHT_YES
            }
        }
        compose.runOnIdle {
            assertEquals(primaryDark, colors?.primary)
            assertEquals(surfaceDark, colors?.surface)
            assertEquals(onSurfaceDark, colors?.onSurface)
            configuration.value = Configuration(configuration.value).apply {
                uiMode = Configuration.UI_MODE_NIGHT_NO
            }
        }
        compose.runOnIdle {
            assertEquals(primaryLight, colors?.primary)
            assertEquals(surfaceLight, colors?.surface)
        }
    }
}
