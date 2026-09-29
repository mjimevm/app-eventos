package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar

@Composable
fun BussinesAccountPage(
    modifier: Modifier = Modifier,
    usuario: String,
    correo: String,
    imagenPerfil: Int? = null,
    numero: String,
    ubicacion: String,
    onBackClick: () -> Unit = {},
    onAjustes: () -> Unit = {},
    onBottomClick: (Int) -> Unit = {},
    listaPublicados: List<ProximamenteItem> = listOf(),
    listaCreados: List<ProximamenteItem> = listOf()
) {
    var seleccionado by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                TopBar(
                    showBackButton = false,
                    onBackClick = onBackClick
                )
            }
            IconButton(onClick = onAjustes) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Ajustes",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Hero Profile Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (imagenPerfil == null) {
                    Surface(
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = usuario.firstOrNull()?.uppercase() ?: "",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Icon(
                        painter = painterResource(id = imagenPerfil),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = usuario,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = correo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (numero.isNotEmpty() || ubicacion.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = listOf(numero, ubicacion).filter { it.isNotEmpty() }.joinToString(" • "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProfileStatItem(count = listaCreados.size, label = "Creados")
                    VerticalDivider(
                        modifier = Modifier.height(24.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ProfileStatItem(count = listaPublicados.size, label = "Publicados")
                }
            }
        }

        // Navigation Tabs Section
        SeleccionBussines(
            seleccionado = seleccionado,
            onTabSelected = { seleccionado = it },
            counts = listOf(listaCreados.size, listaPublicados.size)
        )

        // Event List or Empty State
        val currentList = when (seleccionado) {
            0 -> listaCreados
            1 -> listaPublicados
            else -> emptyList()
        }

        if (currentList.isEmpty()) {
            val emptyMessage = when (seleccionado) {
                0 -> "Aún no has creado ningún evento"
                1 -> "Aún no has publicado ningún evento"
                else -> "No hay eventos para mostrar"
            }
            EmptyStateView(
                message = emptyMessage,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(currentList.size) { index ->
                    val item = currentList[index]
                    ProximamenteCard(
                        titulo = item.title,
                        fecha = item.fecha,
                        dia = item.dia,
                        ubicacion = item.ubicacion,
                        cantidadAmigos = 0,
                        imageResId = item.imageResId
                    )
                }
            }
        }

        BottomNavigationBar(selectedItem = 3, onItemClick = onBottomClick)
    }
}

@Composable
fun SeleccionBussines(
    seleccionado: Int,
    onTabSelected: (Int) -> Unit,
    counts: List<Int>,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        "Creados",
        "Publicados"
    )

    PrimaryTabRow(
        selectedTabIndex = seleccionado,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val count = counts.getOrElse(index) { 0 }
            Tab(
                selected = seleccionado == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                text = {
                    Text(
                        text = "$title",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun BussinesPagePreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        BussinesAccountPage(
            usuario = "Coca Cola",
            correo = "COLA2@gmail.com",
            imagenPerfil = null,
            numero = "+502 4000-2000",
            ubicacion = "Ciudad de Guatemala",
            listaCreados = listOf(
                ProximamenteItem("Festival de las Flores", "NOV", 1, "Antigua Guatemala", R.drawable.festival)
            ),
            listaPublicados = listOf(
                ProximamenteItem("Feria de Noviembre", "NOV", 2, "Ciudad de Guatemala", R.drawable.feria)
            )
        )
    }
}