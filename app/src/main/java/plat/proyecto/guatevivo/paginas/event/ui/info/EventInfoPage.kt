package plat.proyecto.guatevivo.paginas.event.ui.info

import android.content.res.Configuration.UI_MODE_NIGHT_NO
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable
data class EventInfoDestination(val eventId: Int = 0)

fun NavController.navigateToEventInfo(eventId: Int = 0, navOptions: NavOptions? = null) {
    navigate(EventInfoDestination(eventId), navOptions)
}

fun NavGraphBuilder.eventInfoScreen(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBackArrow: () -> Unit
) {
    composable<EventInfoDestination> {
        EventInfoRoute(
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
            onCreateClick = onCreateClick,
            onProfileClick = onProfileClick,
            onBackArrow = onBackArrow
        )
    }
}

@Composable
fun EventInfoRoute(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBackArrow: () -> Unit,
    viewModel: EventInfoViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    EventoInfoPage(
        state = state,
        onEvent = viewModel::onEvent,
        onHomeClick = onHomeClick,
        onSearchClick = onSearchClick,
        onCreateClick = onCreateClick,
        onProfileClick = onProfileClick,
        onBackArrow = onBackArrow
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventoInfoPage(
    state: EventInfoState = EventInfoState(),
    onEvent: (EventInfoEvent) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackArrow: () -> Unit = {}
) {
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        topBar = {
            TopBar(
                showBackButton = true,
                onBackClick = onBackArrow
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = -1,
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
        containerColor = surfaceColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(450.dp)
                ) {
                    Image(
                        painter = painterResource(id = state.imageResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

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
                    state.successMessage?.let { message ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = message,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onEvent(EventInfoEvent.ClearMessage) }) {
                                    Text("Cerrar", color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }

                    Text(
                        text = state.titulo,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.miniDescripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoBox(
                            modifier = Modifier.weight(1f),
                            titulo = state.dia + " de",
                            subtitulo = state.mes

                        )
                        InfoBox(
                            modifier = Modifier.weight(1f),
                            titulo = "Ver mapa",
                            subtitulo = state.ubicacion
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = state.precio,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            AmigosStack(state.amigosAsistiran)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${state.amigosAsistiran.size + 2} Amigos asistirán",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Acerca del Evento",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.descripcionLarga,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            Button(
                onClick = { onEvent(EventInfoEvent.ToggleAttend) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 12.dp, top = 8.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isAttending) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.surface
                    )
                } else {
                    Text(
                        text = if (state.isAttending) "¡Asistiendo! (Cancelar)" else "Asistir",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
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

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EventoInfoPagePreview() {
    GuatevivoTheme {
        EventoInfoPage()
    }
}
