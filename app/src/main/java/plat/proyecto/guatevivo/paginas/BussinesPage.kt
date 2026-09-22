package plat.proyecto.guatevivo.paginas

import android.text.Layout
import android.widget.Button
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun BussinesAccountPage(modifier: Modifier = Modifier,
                usuario: String,
                correo: String,
                imagenPerfil: Int,
                numero: String,
                ubicacion: String,
                onBackClick: () -> Unit = {},
                onAjustes: () -> Unit = {},
                onBottomClick: (Int) -> Unit = {},
                listaPublicados: List<ProximamenteItem> = listOf(),
                listaCreados: List<ProximamenteItem> = listOf(),
) {
    var seleccionado by remember { mutableIntStateOf(0) }
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                TopBar(
                    showBackButton = true,
                    onBackClick = onBackClick
                )
            }
            IconButton(
                onClick = onAjustes
            ) {
                Image(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = "Ajustes"
                )
            }
        }

        Surface (modifier = modifier
            .fillMaxWidth()
            .padding(15.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(5.dp)
            ),
        ) {
            Column(modifier = modifier
                .fillMaxWidth()
                .padding(15.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {
                Icon(
                    modifier = modifier.size(60.dp),
                    painter = painterResource(id = imagenPerfil),
                    contentDescription = null
                )
                Text(usuario,
                    style = MaterialTheme.typography.titleLarge)
                Text(correo,
                    style = MaterialTheme.typography.bodyMedium)
                Text(numero,
                    style = MaterialTheme.typography.bodyMedium)
                Text(ubicacion,
                    style = MaterialTheme.typography.bodyMedium)
            }

        }

        SeleccionBussines(
            onCreadosClick = {seleccionado = 0},
            onPublicadosClick = {seleccionado = 1},
        )
        if (seleccionado == 0){
            LazyColumn(modifier = modifier.weight(1f)
            ) {
                items(listaCreados.count()) {
                    ProximamenteCard(listaCreados[it].title, listaCreados[it].fecha, listaCreados[it].dia, listaCreados[it].ubicacion, 0, listaCreados[it].imageResId)
                }
            }
        } else if (seleccionado == 1){
            LazyColumn(modifier = modifier.weight(1f)
            ) {
                items(listaPublicados.count()) {
                    ProximamenteCard(listaPublicados[it].title, listaPublicados[it].fecha, listaPublicados[it].dia, listaPublicados[it].ubicacion, 0, listaPublicados[it].imageResId)
                }
            }
        } else {
            Spacer(modifier = modifier.weight(1f))
        }

        BottomNavigationBar(selectedItem = 3, onItemClick = onBottomClick)
    }
}

@Composable
fun SeleccionBussines(
    onPublicadosClick: () -> Unit,
    onCreadosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var seleccionado by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Creados",
        "Publicados"
    )

    PrimaryTabRow(
        selectedTabIndex = seleccionado,
        modifier = modifier.fillMaxWidth()
    ) {

        tabs.forEachIndexed { index, title ->
            Tab(
                selected = seleccionado == index,
                onClick = {
                    seleccionado = index
                    when (index) {
                        0 -> onCreadosClick()
                        1 -> onPublicadosClick()
                    }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                text = {
                    Text(title)
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BussinesPagePreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        BussinesAccountPage(
            usuario = "Andres",
            correo ="pin25212@gmail.com",
            imagenPerfil = R.drawable.account_circle,
            numero = "+502 4000-2000",
            ubicacion = "Ciudad de Guatemala",
            listaCreados = listOf(
                ProximamenteItem("Festival de las Flores", "NOV", 1, "Antigua Guatemala", R.drawable.festival)),
            listaPublicados = listOf(
                ProximamenteItem("Festival de las Flores", "NOV", 2, "Antigua Guatemala", R.drawable.festival)),
            )
    }
}