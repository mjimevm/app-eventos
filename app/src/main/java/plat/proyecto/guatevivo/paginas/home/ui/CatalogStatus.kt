package plat.proyecto.guatevivo.paginas.home.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CatalogStatus(isLoading: Boolean, isFromCache: Boolean, error: String?, onRefresh: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (isFromCache) Text(
            "Sin conexión. Mostrando eventos guardados; pueden haber cambiado.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        TextButton(enabled = !isLoading, onClick = onRefresh) {
            Text(if (error != null || isFromCache) "Reintentar" else "Actualizar")
        }
    }
}
