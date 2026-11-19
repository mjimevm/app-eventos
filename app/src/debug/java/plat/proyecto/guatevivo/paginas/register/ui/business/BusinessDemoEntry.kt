package plat.proyecto.guatevivo.paginas.register.ui.business

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

/** Debug-only isolated preview. No repositories, Firebase session or network operations. */
@Composable
fun BusinessDemoEntry() {
    var open by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { open = true }) { Text("Abrir perfil empresarial DEMO (local)") }
    if (open) {
        val profile = remember {
            UserDto(uid = "demo-local-business", name = "Empresa ficticia GuateVivo DEMO",
                email = "empresa@example.invalid", phone = "00000000", address = "Dirección ficticia",
                description = "Perfil empresarial de demostración", accountType = AccountType.BUSINESS.name)
        }
        Dialog(onDismissRequest = { open = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("DEMO · SOLO DEBUG", style = MaterialTheme.typography.headlineMedium)
                    Text("Perfil local ficticio. No se creó ninguna cuenta ni se guardaron datos en Firebase.")
                    Text(profile.name, style = MaterialTheme.typography.titleLarge)
                    Text("Tipo de cuenta: ${profile.accountType}")
                    Text(profile.email)
                    Text(profile.phone)
                    Text(profile.address)
                    Text(profile.description)
                    Text("Eventos creados: 0 · Eventos publicados: 0")
                    Button(onClick = { open = false }) { Text("Salir de DEMO y volver al registro real") }
                }
            }
        }
    }
}
