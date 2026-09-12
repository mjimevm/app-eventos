package plat.proyecto.guatevivo.bars

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R

@Composable
fun TopBar(
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (showBackButton) {
            IconButton(onClick = onBackClick) {
                Icon(
                    painter = painterResource(id = R.drawable.return_icon),
                    contentDescription = "Regresar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.guatevivo),
            contentDescription = "Guatevivo Logo",
            modifier = Modifier.height(40.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun topBarPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        TopBar()
    }
}
