package plat.proyecto.guatevivo.paginas.home.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.core.bars.BottomNavigationBar
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteCard
import plat.proyecto.guatevivo.paginas.home.ui.home.SearchBarSection
import plat.proyecto.guatevivo.paginas.home.ui.home.SectionHeader
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable
data object SearchDestination

fun NavController.navigateToSearch(navOptions: NavOptions? = null) {
    navigate(SearchDestination, navOptions)
}

fun NavGraphBuilder.searchScreen(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEvent: (Int) -> Unit
) {
    composable<SearchDestination> {
        SearchRoute(
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
            onCreateClick = onCreateClick,
            onProfileClick = onProfileClick,
            onEvent = onEvent
        )
    }
}

@Composable
fun SearchRoute(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEvent: (Int) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    SearchPage(
        state = state,
        onEvent = viewModel::onEvent,
        onHomeClick = onHomeClick,
        onSearchClick = onSearchClick,
        onCreateClick = onCreateClick,
        onProfileClick = onProfileClick,
        onEventClick = onEvent
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchPage(
    modifier: Modifier = Modifier,
    state: SearchState = SearchState(),
    onEvent: (SearchEvent) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEventClick: (Int) -> Unit = {}
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopBar(showBackButton = false)
        },
        containerColor = MaterialTheme.colorScheme.surface,
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 1,
                onItemClick = { index ->
                    when (index) {
                        0 -> onHomeClick()
                        1 -> onSearchClick()
                        2 -> onCreateClick()
                        3 -> onProfileClick()
                    }
                },
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
            // 1. Barra de Búsqueda
            item {
                SearchBarSection(
                    query = state.searchQuery,
                    onQueryChange = { query -> onEvent(SearchEvent.UpdateQuery(query)) },
                    text = "Busca eventos, lugares u artistas"
                )
            }

            // 2. Filtros de Categorías adaptados (FlowRow)
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.categories.forEach { category ->
                        val isSelected = category == state.selectedCategory
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                            onClick = { onEvent(SearchEvent.SelectCategory(category)) },
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

            // 3. Sección Sugeridos o Resultados
            item {
                val headerTitle = if (state.searchQuery.isBlank()) "Sugeridos" else "Resultados para \"${state.searchQuery}\""
                SectionHeader(title = headerTitle, hasAction = state.searchQuery.isBlank())
            }

            // 4. Tarjetas Próximamente / Resultados
            items(state.searchResults) { item ->
                ProximamenteCard(
                    titulo = item.titulo,
                    fecha = item.fecha,
                    dia = item.dia,
                    ubicacion = item.ubicacion,
                    cantidadAmigos = item.cantidadAmigos,
                    imageResId = item.imageResId,
                    onEvent = { onEventClick(item.id) }
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
