package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchPage(
    modifier: Modifier = Modifier,
    onBottomClick: (Int) -> Unit = {},
) {
    var selectedCategory by remember { mutableStateOf("Populares") }

    val categories = listOf(
        "Populares", "Conciertos", "Gastronomía",
        "Cultura", "Arte", "Danza",
        "Exhibiciones", "Religión", "Política"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(showBackButton = false)
        },
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 1,
                onItemClick = onBottomClick,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            item {
                Text(
                    "Buscar",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )
            }
            // 1. Barra de Búsqueda (mismo componente del Home)
            item {
                SearchBarSection(text = "Busca eventos, lugares u artistas")
            }

            // 2. Filtros de Categorías adaptados a la longitud de la palabra (FlowRow)
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            onClick = { selectedCategory = category },
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }

            // 3. Sección Sugeridos (mismo componente del Home)
            item {
                SectionHeader(title = "Sugeridos", hasAction = true)
            }

            // 4. Tarjetas Próximamente (mismo componente ProximamenteCard del Home)
            item {
                ProximamenteCard(
                    titulo = "Festival de Barriletes Gigantes",
                    fecha = "NOV",
                    dia = 1,
                    ubicacion = "Sumpango, Sacatepéquez",
                    cantidadAmigos = 2,
                    imageResId = R.drawable.festival,
                )
            }
            item {
                ProximamenteCard(
                    titulo = "Cena de Café en Atitlán",
                    fecha = "SEP",
                    dia = 23,
                    ubicacion = "Panajachel, Atitlán",
                    cantidadAmigos = 2,
                    imageResId = R.drawable.panajachel,
                )
            }
        }
    }
}
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SearchPreview() {
    GuatevivoTheme {
        SearchPage()
    }
}
