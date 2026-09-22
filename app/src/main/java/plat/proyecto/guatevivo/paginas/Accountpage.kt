package plat.proyecto.guatevivo.paginas

import android.text.Layout
import android.widget.Button
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar


@Composable
fun AccountPage(modifier: Modifier = Modifier,
                usuario: String,
                correo: String,
                onBackClick: () -> Unit = {},
                onBottomClick: (Int) -> Unit = {},
                onAmistades: () -> Unit = {}) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(showBackButton = true, onBackClick = onBackClick)

        Surface (modifier = modifier
            .fillMaxWidth()
            .padding(15.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(5.dp)
            ),
            tonalElevation = 2.dp,
        ) {
            Column(modifier = modifier
                .fillMaxWidth()
                .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                ) {
                Icon(
                    painter = painterResource(id = R.drawable.account_circle),
                    contentDescription = null
                )
                Text(usuario,
                    style = MaterialTheme.typography.bodyLarge)
                Text(correo,
                    style = MaterialTheme.typography.bodySmall)
            }

        }

        Surface(
            onClick = onAmistades,
            tonalElevation = 2.dp,
            modifier = modifier
                .fillMaxWidth()
                .padding(15.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(5.dp)
                ),
        ) {
            Text("Amistades",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
        }

        Seleccion(
            selectedItem = 0,
            onItemClick = {})

        Asistiendo()

        BottomNavigationBar(selectedItem = 3, onItemClick = onBottomClick)
    }
}

@Composable
fun Seleccion(modifier: Modifier = Modifier,
              selectedItem: Int = 0,
              onItemClick: (Int) -> Unit = {}){
    NavigationBar(
        modifier = modifier.height(40.dp).fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        val items = listOf(
            Pair("Asistiendo", 0),
            Pair("Creados", 1),
            Pair("Publicados",2),
        )

        items.forEach { (label, index) ->
            val isSelected = selectedItem == index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(index) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                alwaysShowLabel = true,
                icon = {},
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}


@Composable
fun Asistiendo(modifier: Modifier = Modifier){
    LazyColumn(modifier = modifier
    ) {
        items(1) {
            ProximamenteCard("Festival de las Flores", "NOV", 1, "Antigua Guatemala", 0, R.drawable.festival)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AccountPagePreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        AccountPage(usuario = "Andres",correo ="pin25212@gmail.com",)
    }
}