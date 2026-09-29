package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Composable
fun SettingsPage(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {},
    onDarkModeChanged: (Boolean) -> Unit = {},
) {
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopBar(true)
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = -1)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp),
            )

            // Sección: APARIENCIA
            SettingsCategorySection(title = "APARIENCIA") {
                SettingsToggleRowItem(
                    iconRes = R.drawable.settings,
                    title = "Modo oscuro",
                    subtitle = "Cambiar entre tema claro y oscuro",
                    checked = darkModeEnabled,
                    onCheckedChange = {
                        darkModeEnabled = it
                        onDarkModeChanged(it)
                    },
                )
            }

            // Sección: UBICACIÓN Y REGIÓN
            SettingsCategorySection(title = "UBICACIÓN") {
                SettingsRowItem(
                    iconRes = R.drawable.location_icon,
                    title = "Ubicación y regiones",
                    subtitle = "Guatemala, Antigua, Xela y más",
                    onClick = onLocationClick,
                )
            }

            // Sección: SOPORTE Y GENERAL
            SettingsCategorySection(title = "SOPORTE Y GENERAL") {
                SettingsRowItem(
                    iconRes = R.drawable.favorite,
                    title = "Ayuda y Preguntas Frecuentes",
                    subtitle = "Soporte oficial de Guatevivo",
                    onClick = onHelpClick,
                )
                SettingsRowItem(
                    iconRes = R.drawable.company_icon,
                    title = "Acerca de Guatevivo",
                    subtitle = "Versión 1.0.0 • Hecho en Guatemala 🇬🇹",
                    onClick = onAboutClick,
                )
            }

            // Sección: SESIÓN
            SettingsCategorySection(title = "SESIÓN") {
                SettingsRowItem(
                    iconRes = R.drawable.lock_icon,
                    title = "Cerrar sesión",
                    subtitle = "Salir de tu cuenta actual",
                    onClick = onLogoutClick,
                )
                SettingsRowItem(
                    iconRes = R.drawable.add_circle,
                    title = "Eliminar cuenta",
                    subtitle = "Borrar permanentemente tus datos",
                    onClick = onDeleteAccountClick,
                )
            }
        }
    }
}

@Composable
fun SettingsCategorySection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                lineHeight = 16.sp,
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp),
            letterSpacing = 1.2.sp,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRowItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
fun SettingsRowItem(
    iconRes: Int,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.return_icon),
                contentDescription = "Ir",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(14.dp)
                    .rotate(180f),
            )
        }
    }
}
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SettingsPreview() {
    GuatevivoTheme {
        SettingsPage()
    }
}
