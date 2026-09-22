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
fun AccountPage(modifier: Modifier = Modifier,
                usuario: String,
                correo: String,
                imagenPerfil: Int,
                onBackClick: () -> Unit = {},
                onAjustes: () -> Unit = {},
                onBottomClick: (Int) -> Unit = {},
                onAmistades: () -> Unit = {},
                listaAsistidos: List<ProximamenteItem> = listOf(),
                listaCreados: List<ProximamenteItem> = listOf(),
                listaCompartidos: List<ProximamenteItem> = listOf()
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
            }

        }

        Surface(
            onClick = onAmistades,
            modifier = modifier
                .fillMaxWidth()
                .padding(15.dp)
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.primary,
                    RoundedCornerShape(5.dp)
                ),
        ) {
            Text("Amistades",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center)
        }

        SeleccionUsuario(
            onAsistiendoClick = {seleccionado = 0},
            onCreadosClick = {seleccionado = 1},
            onCompartidosClick = {seleccionado = 2}
        )
        if (seleccionado == 0){
            LazyColumn(modifier = modifier.weight(1f)
            ) {
                items(listaAsistidos.count()) {
                    ProximamenteCard(listaAsistidos[it].title, listaAsistidos[it].fecha, listaAsistidos[it].dia, listaAsistidos[it].ubicacion, 0, listaAsistidos[it].imageResId)
                }
            }
        } else if (seleccionado == 1){
            LazyColumn(modifier = modifier.weight(1f)
            ) {
                items(listaCreados.count()) {
                    ProximamenteCard(listaAsistidos[it].title, listaAsistidos[it].fecha, listaAsistidos[it].dia, listaAsistidos[it].ubicacion, 0, listaAsistidos[it].imageResId)
                }
            }
        } else if (seleccionado == 2){
            LazyColumn(modifier = modifier.weight(1f)
            ) {
                items(listaCompartidos.count()) {
                    ProximamenteCard(listaAsistidos[it].title, listaAsistidos[it].fecha, listaAsistidos[it].dia, listaAsistidos[it].ubicacion, 0, listaAsistidos[it].imageResId)
                }
            }
        } else {
            Spacer(modifier = modifier.weight(1f))
        }

        BottomNavigationBar(selectedItem = 3, onItemClick = onBottomClick)
    }
}

@Composable
fun SeleccionUsuario(
    onAsistiendoClick: () -> Unit,
    onCreadosClick: () -> Unit,
    onCompartidosClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var seleccionado by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Asistiendo",
        "Creados",
        "Compartidos"
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
                        0 -> onAsistiendoClick()
                        1 -> onCreadosClick()
                        2 -> onCompartidosClick()
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
fun AccountPageAsistidosPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        AccountPage(usuario = "Andres",correo ="pin25212@gmail.com", imagenPerfil = R.drawable.account_circle,
            listaAsistidos = listOf(
            ProximamenteItem("Festival de las Flores", "NOV", 1, "Antigua Guatemala", R.drawable.festival)),
            listaCreados = listOf(
                ProximamenteItem("Festival de las Flores", "NOV", 2, "Antigua Guatemala", R.drawable.festival)),
            listaCompartidos = listOf(
                ProximamenteItem("Festival de las Flores", "NOV", 3, "Antigua Guatemala", R.drawable.festival))
        )
    }
}