package plat.proyecto.guatevivo.bars

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import plat.proyecto.guatevivo.R

@Composable
fun BottomNavigationBar(
    selectedItem: Int = 0,
    onItemClick: (Int) -> Unit = {}
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        val items = listOf(
            Triple("Inicio", R.drawable.favorite, 0),
            Triple("Buscar", R.drawable.search, 1),
            Triple("Crear", R.drawable.add_circle, 2),
            Triple("Perfil", R.drawable.account_circle, 3)
        )

        items.forEach { (label, iconRes, index) ->
            val isSelected = selectedItem == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(index) },
                label = { Text(label) },
                icon = {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = label,
                        // AQUÍ: Si está seleccionado usa el color contrario (onSurface)
                        // Si no, usa un color variante para que se note la diferencia
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.secondary,
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
