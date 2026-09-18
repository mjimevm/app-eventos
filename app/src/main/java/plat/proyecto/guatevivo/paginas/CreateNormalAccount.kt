package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.bars.TopBarCenter

@Composable
fun CreateNormalAccount(modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxSize()
        .padding(25.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start)
    {
        TopBarCenter()

        Text("Crear Cuenta",
            style = MaterialTheme.typography.displayMedium)

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp, bottom = 30.dp),
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Row(modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ){
            OutlinedTextField(
                modifier = modifier.weight(1f),
                state = rememberTextFieldState(),
                shape = RoundedCornerShape(5.dp),
                label = { Text("Nombre") },
            )

            OutlinedTextField(
                modifier = modifier.weight(1f),
                state = rememberTextFieldState(),
                shape = RoundedCornerShape(5.dp),
                label = { Text("Apellido") },
            )
        }

        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            state = rememberTextFieldState(),
            shape = RoundedCornerShape(5.dp),
            label = { Text("Correo Electrónico") },
        )

        OutlinedSecureTextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            state = rememberTextFieldState(),
            shape = RoundedCornerShape(5.dp),
            label = { Text("Contraseña") },
        )

        OutlinedSecureTextField(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            state = rememberTextFieldState(),
            shape = RoundedCornerShape(5.dp),
            label = { Text("Confirmar Contraseña") },
        )

        Row(modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically)
        {
            Checkbox(checked = false, onCheckedChange = {})
            Text("Acepto los Términos de uso y la Política de Privacidad")
        }

        Spacer(modifier = modifier.weight(1f))

        FilledTonalButton(onClick = {},
            modifier = modifier
                .padding(bottom = 15.dp)
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(5.dp),
            colors = ButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) { Text("Crear Cuenta",
            style = MaterialTheme.typography.bodyLarge
        )}
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun CreateNormalAccountPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        CreateNormalAccount()
    }
}