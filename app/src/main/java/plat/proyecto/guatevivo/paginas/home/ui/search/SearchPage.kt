package plat.proyecto.guatevivo.paginas.home.ui.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.painterResource
import plat.proyecto.guatevivo.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.core.bars.BottomNavigationBar
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.paginas.home.ui.CatalogStatus
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
    onEvent: (String) -> Unit
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
    onEvent: (String) -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

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
    onEventClick: (String) -> Unit = {}
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
                            enabled = false,
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
                if (
                    state.searchQuery.isNotBlank() ||
                    state.selectedCategory != "Todos"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                onEvent(SearchEvent.ResetFilters)
                            }
                        ) {
                            Text(
                                text = "Restablecer filtros",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // 3. Sección Sugeridos o Resultados
            item {
                val headerTitle = if (state.searchQuery.isBlank()) {
                    "Sugeridos"
                } else {
                    "Resultados para \"${state.searchQuery}\""
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 8.dp,
                            bottom = 8.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = headerTitle,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = {
                            onEvent(SearchEvent.Refresh)
                        },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.refresh
                                ),
                                contentDescription = "Actualizar eventos",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            // Mensaje cuando no hay eventos
            if (
                !state.isLoading &&
                state.error == null &&
                state.searchResults.isEmpty()
            ) {
                item {
                    Text(
                        text = if (state.searchQuery.isBlank()) {
                            "No hay próximos eventos públicos."
                        } else {
                            "No se encontraron coincidencias."
                        },
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 4. Tarjetas Próximamente / Resultados
            items(state.searchResults, key = { it.eventId }) { item ->
                ProximamenteCard(
                    titulo = item.titulo,
                    fecha = item.fecha,
                    dia = item.dia,
                    ubicacion = item.ubicacion,
                    cantidadAmigos = item.cantidadAmigos,
                    imageResId = item.imageResId,
                    imageUrl = item.imageUrl,
                    fullDate = item.fullDate,
                    hora = item.hora,
                    onEvent = { onEventClick(item.eventId) }
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
