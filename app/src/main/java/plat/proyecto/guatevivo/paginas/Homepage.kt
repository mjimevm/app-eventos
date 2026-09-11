package plat.proyecto.guatevivo.paginas

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
    val title: String,
    val location: String
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestacadosCarousel() {
    val items = listOf(
        CarouselItem(0, R.drawable.festival, "Festival de las Flores", "Antigua Guatemala"),
        CarouselItem(1, R.drawable.feria, "Concierto en Atitlán", "Panajachel, Sololá"),
        CarouselItem(2, R.drawable.panajachel, "Feria de noviembre", "Ciudad de Guatemala"),
    )

    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { items.count() },
        preferredItemWidth = 330.dp,
        maxSmallItemWidth = 40.dp,
        itemSpacing = 8.dp,
        modifier = Modifier
            .width(372.dp)
            .height(328.dp)
            .padding(horizontal = 16.dp),
    ) { i ->
        val item = items[i]

        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = item.imageResId),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 600f // Comienza más abajo para no cubrir la foto
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.location,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    Scaffold(
        topBar = {
            TopBar()
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
                CategoryFilters()
            }

            // 3. Sección Destacados
            item {
                SectionHeader(title = "Destacados")
            }
            item {
                DestacadosCarousel()
            }

            // 4. Sección Próximamente
            item {
                SectionHeader(title = "Próximamente", hasAction = true)
            }

            // Lista de eventos próximos (ejemplo)
            items(2) {
                ProximamenteCard()
            }
        }
    }
}

@Composable
fun SearchBarSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 5.dp,
                    spotColor = Color(0x26000000),
                    ambientColor = Color(0x26000000),
                    shape = RoundedCornerShape(4.dp)
                )
                .width(372.dp)
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
}

@Composable
fun CategoryFilters() {
    val categories = listOf("Todos", "Conciertos", "Cultura", "Gastronomía", "Moda")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == "Todos"
            Button(
                onClick = { },
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
            .padding(16.dp),
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
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ProximamenteCard() {
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
                    painter = painterResource(id = R.drawable.festival),
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
                        text = "Festival de Barriletes Gigantes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Sumpango, Sacatepéquez",
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
                            text = "NOV",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "1",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    }
                }
            }

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
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
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
                    text = "2 Amigos asistirán",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
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
        ProximamenteCard()
    }
}
