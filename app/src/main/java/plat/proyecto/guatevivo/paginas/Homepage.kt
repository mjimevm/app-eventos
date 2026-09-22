package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar


data class CarouselItem (
    val id: Int,
    @DrawableRes val imageResId: Int,
    val titulo: String,
    val ubicacion: String
)

@Composable
fun DestacadosCard(
    titulo: String,
    ubicacion: String,
    @DrawableRes imageResId: Int
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = titulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestacadosCarousel(items: List<CarouselItem>) {
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
            imageResId = item.imageResId
        )
    }
}

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopBar(false)
        },
        bottomBar = {
            BottomNavigationBar()
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
                SearchBarSection()
            }

            // 2. Filtros de Categorías
            item {
                val categories = listOf("Todos", "Conciertos", "Cultura", "Gastronomía", "Moda")
                CategoryFilters(
                    categories = categories,
                    onCategorySelected = { /* TODO */ }
                )
            }

            // 3. Sección Destacados
            item {
                SectionHeader(title = "Destacados")
            }
            item {
                val destacadosItems = listOf(
                    CarouselItem(0, R.drawable.festival, "Festival de las Flores", "Antigua Guatemala"),
                    CarouselItem(1, R.drawable.feria, "Concierto en Atitlán", "Panajachel, Sololá"),
                    CarouselItem(2, R.drawable.panajachel, "Feria de noviembre", "Ciudad de Guatemala"),
                )
                DestacadosCarousel(items = destacadosItems)
            }

            // 4. Sección Próximamente
            item {
                SectionHeader(title = "Próximamente", hasAction = true)
            }

            // Lista de eventos próximos (ejemplo)
            items(2) {
                ProximamenteCard("Festival de las Flores", "NOV", 1, "Antigua Guatemala", 0, R.drawable.festival)
            }
        }
    }
}

@Composable
fun SearchBarSection() {
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
            Text(
                text = "Busca eventos, lugares u artistas",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
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
    onCategorySelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == "Todos"
            Button(
                onClick = { onCategorySelected(category) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
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

data class ProximamenteItem(
    val title: String,
    val fecha: String,
    val dia: Int,
    val ubicacion: String,
    val imageResId: Int
)

@Composable
fun ProximamenteCard(
    titulo: String,
    fecha: String,
    dia: Int,
    ubicacion: String,
    cantidadAmigos: Int,
    @DrawableRes imageResId: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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
                    Box(modifier = Modifier.height(32.dp).width(52.dp)) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.account_circle),
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
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.account_circle),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Text(
                        text = if (cantidadAmigos > 1) "$cantidadAmigos amigos asistirán" else "$cantidadAmigos amigo asistirá",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun HomePreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme  {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ProximamenteCardPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        ProximamenteCard("Festival de las Flores", "NOV", 1, "Antigua Guatemala", 1, R.drawable.festival)
    }
}
