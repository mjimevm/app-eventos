package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventoInfoPage(
    titulo: String = "Festival de las Flores",
    miniDescripcion: String = "La celebración anual más grande del arte y la música en la ciudad colonial.",
    fecha: String = "14 y 15 de Noviembre",
    ubicacion: String = "Calle del Arco",
    precio: String = "Entrada Gratis",
    descripcionLarga: String = "El Festival de las Flores es un evento cultural y artístico anual que transforma por completo las calles coloniales de La Antigua Guatemala en un gigantesco lienzo botánico. Durante un fin de semana, la arquitectura barroca de la ciudad se fusiona con miles de colores y aromas naturales, atrayendo a miles de visitantes locales e internacionales.",
    amigosAsistiran: List<Int> = listOf(R.drawable.account_circle, R.drawable.account_circle, R.drawable.account_circle)
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopBar(
                showBackButton = true,
                onBackClick = { /* TODO: Navegar hacia atrás */ }
            )
        },
        bottomBar = {
            BottomNavigationBar()
        },
        containerColor = surfaceColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Contenido Scrollable
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Imagen con Gradientes
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.festival), // Usa tu recurso aquí
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradiente Superior (para mezcla con TopBar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(surfaceColor, Color.Transparent)
                                )
                            )
                    )

                    // Gradiente Inferior (para que la imagen "desaparezca" en el surface)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .height(150.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, surfaceColor)
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = titulo,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = miniDescripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Fila de Fecha y Mapa
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoBox(
                            modifier = Modifier.weight(1f),
                            titulo = fecha,
                            subtitulo = "Noviembre"

                        )
                        InfoBox(
                            modifier = Modifier.weight(1f),
                            titulo = "Ver mapa",
                            subtitulo = ubicacion
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Box de Precio
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = precio,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Box de Amigos
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            AmigosStack(amigosAsistiran)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${amigosAsistiran.size + 2} Amigos asistirán",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Acerca del evento
                    Text(
                        text = "Acerca del Evento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = descripcionLarga,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // Botón Asistir (Pegado al final con padding seguro)
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp, top = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Asistir",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun InfoBox(modifier: Modifier, titulo: String, subtitulo: String) {
    Surface(
        modifier = modifier.height(80.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = titulo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AmigosStack(amigos: List<Int>) {
    Box(modifier = Modifier.width((amigos.size * 20 + 10).dp)) {
        amigos.forEachIndexed { index, iconRes ->
            Surface(
                modifier = Modifier
                    .offset(x = (index * 20).dp)
                    .size(32.dp)
                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EventoInfoPagePreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        EventoInfoPage()
    }
}

