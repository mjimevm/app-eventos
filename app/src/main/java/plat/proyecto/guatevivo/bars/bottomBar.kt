package plat.proyecto.guatevivo.bars

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R

@Composable
fun BottomNavigationBar(
    selectedItem: Int = 0,
    onItemClick: (Int) -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets // Esto maneja automáticamente el área de gestos
    ) {
        val items = listOf(
            Triple("Inicio", R.drawable.lucide_house, 0),
            Triple("Buscar", R.drawable.search, 1),
            Triple("Crear", R.drawable.add_circle, 2),
            Triple("Perfil", R.drawable.account_circle, 3)
        )

        items.forEach { (label, iconRes, index) ->
            val isSelected = selectedItem == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(index) },
                label = { 
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ) 
                },
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun bottomBarPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        BottomNavigationBar()
    }
}
