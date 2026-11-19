package plat.proyecto.guatevivo.paginas.home.ui.home

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.core.bars.BottomNavigationBar
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.paginas.home.ui.CatalogStatus
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable
data object HomeDestination

fun NavController.navigateToHome(navOptions: NavOptions? = null) {
    navigate(HomeDestination, navOptions)
}

fun NavGraphBuilder.homeScreen(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEvent: (String) -> Unit
) {
    composable<HomeDestination> {
        HomeRoute(
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
            onCreateClick = onCreateClick,
            onProfileClick = onProfileClick,
            onEvent = onEvent
        )
    }
}

@Composable
fun HomeRoute(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onEvent: (String) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    HomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onHomeClick = onHomeClick,
        onSearchClick = onSearchClick,
        onCreateClick = onCreateClick,
        onProfileClick = onProfileClick,
        onEventClick = onEvent
    )
}

@Composable
fun DestacadosCard(
    onEvent: () -> Unit,
    titulo: String,
    ubicacion: String,
    @DrawableRes imageResId: Int,
    imageUrl: String = "",
    fechaHora: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = true, onClick = onEvent),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)) {
            if (imageUrl.isNotBlank()) AsyncImage(
                model = imageUrl,
                contentDescription = titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            else if (imageResId != 0) Image(
                painter = painterResource(id = imageResId), contentDescription = titulo,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .height(160.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = titulo,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = ubicacion,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (fechaHora.isNotBlank()) Text(text = fechaHora, color = Color.White, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestacadosCarousel(items: List<CarouselItem>, onEventClick: (String) -> Unit) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.count() },
        preferredItemWidth = 330.dp,
        maxSmallItemWidth = 40.dp,
        itemSpacing = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(328.dp)
            .padding(horizontal = 16.dp),
    ) { i ->
        val item = items[i]
        DestacadosCard(
            titulo = item.titulo,
            ubicacion = item.ubicacion,
            imageResId = item.imageResId,
            imageUrl = item.imageUrl,
            fechaHora = item.fechaHora,
            onEvent = { onEventClick(item.eventId) }
        )
    }
}

@Composable
private fun HomeScreen(
    state: HomeState = HomeState(),
    onEvent: (HomeEvent) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onEventClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopBar(false)
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 0,
                onItemClick = { index ->
                    when (index) {
                        0 -> onHomeClick()
                        1 -> onSearchClick()
                        2 -> onCreateClick()
                        3 -> onProfileClick()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Barra de Búsqueda
            item {
                SearchBarSection(query = state.searchQuery, onQueryChange = { onEvent(HomeEvent.UpdateQuery(it)) })
            }

            // 2. Filtros de Categorías
            item {
                CategoryFilters(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = { category -> onEvent(HomeEvent.SelectCategory(category)) },
                    isCategoryAvailable = { it == "Todos" }
                )
                if (state.searchQuery.isNotEmpty()) TextButton(onClick = { onEvent(HomeEvent.ResetFilters) }) {
                    Text(
                        text = "Restablecer búsqueda",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }


            // 3. Sección Destacados
            if (state.destacadosItems.isNotEmpty()) item {
                SectionHeader(title = "Destacados")
            }
            if (state.destacadosItems.isNotEmpty()) item {
                DestacadosCarousel(items = state.destacadosItems, onEventClick = onEventClick)
            }

            // 4. Sección Próximamente
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        ),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Próximamente",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = { onEvent(HomeEvent.Refresh) },
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.refresh),
                                contentDescription = "Actualizar eventos",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Mensaje debajo de Próximamente
            if (!state.isLoading &&
                state.error == null &&
                state.proximamenteItems.isEmpty()
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

            // Lista de eventos próximos
            items(state.proximamenteItems, key = { it.eventId }) { item ->
                ProximamenteCard(
                    onEvent = { onEventClick(item.eventId) },
                    titulo = item.titulo,
                    fecha = item.fecha,
                    dia = item.dia,
                    ubicacion = item.ubicacion,
                    cantidadAmigos = item.cantidadAmigos,
                    imageResId = item.imageResId,
                    imageUrl = item.imageUrl,
                    fullDate = item.fullDate,
                    hora = item.hora
                )
            }
        }
    }
}

@Composable
fun SearchBarSection(
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    text: String = "Busca eventos, lugares u artistas"
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(
                elevation = 5.dp,
                spotColor = Color.Black,
                ambientColor = Color.Black,
                shape = RoundedCornerShape(4.dp)
            )
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) TextButton(onClick = { onQueryChange("") }) { Text("Limpiar") }
            Icon(
                painter = painterResource(id = R.drawable.search),
                contentDescription = "Buscar",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryFilters(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    isCategoryAvailable: (String) -> Boolean = { true }
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Button(
                onClick = { onCategorySelected(category) },
                enabled = isCategoryAvailable(category),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(4.dp),
                border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(text = category)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, hasAction: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (hasAction) {
            Text(
                text = "Ver todos",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
    }
}
@Composable
fun ProximamenteCard(
    onEvent: () -> Unit = {},
    titulo: String,
    fecha: String,
    dia: Int,
    ubicacion: String,
    cantidadAmigos: Int,
    @DrawableRes imageResId: Int,
    imageUrl: String = "",
    fullDate: String = "",
    hora: String = ""
) {
    val tieneImagen = imageUrl.isNotBlank() || imageResId != 0

    Card(
        modifier = Modifier
            .clickable(enabled = true, onClick = onEvent)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column {

            // Mostrar este espacio SOLO si hay imagen
            if (tieneImagen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Información original del evento
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = ubicacion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (fullDate.isNotBlank() || hora.isNotBlank()) {
                        Text(
                            text = listOf(fullDate, hora)
                                .filter { it.isNotBlank() }
                                .joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = fecha,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = dia.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }

            // Sección original de amigos
            if (cantidadAmigos != 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(52.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.account_circle
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .padding(start = 20.dp)
                                .size(32.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = R.drawable.account_circle
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Text(
                        text = if (cantidadAmigos > 1) {
                            "$cantidadAmigos amigos asistirán"
                        } else {
                            "$cantidadAmigos amigo asistirá"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomePreview() {
    GuatevivoTheme  {
        HomeScreen()
    }
}
