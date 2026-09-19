package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.TopBar
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBussinesAccount(
    modifier: Modifier = Modifier
) {
    val nombreEmpresaState = rememberTextFieldState()
    val emailEmpresarialState = rememberTextFieldState()
    val telefonoState = rememberTextFieldState()
    val direccionState = rememberTextFieldState()
    val descripcionState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    var aceptoTerminos by remember { mutableStateOf(false) }
    var esRepresentante by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(showBackButton = true)
        },
        containerColor = MaterialTheme.colorScheme.surface
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
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Impulsa tu negocio",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Crea una cuenta empresarial para publicar y gestionar tus eventos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, outlineColor)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            InputField(
                                label = "Nombre de la Empresa",
                                state = nombreEmpresaState
                            )

                            InputField(
                                label = "Correo Empresarial",
                                state = emailEmpresarialState
                            )

                            InputField(
                                label = "Teléfono de Contacto",
                                state = telefonoState
                            )

                            InputField(
                                label = "Dirección",
                                state = direccionState
                            )

                            InputField(
                                label = "Descripción",
                                state = descripcionState
                            )

                            InputField(
                                label = "Contraseña",
                                state = passwordState,
                                isPassword = true,
                                icon = R.drawable.lock_icon
                            )

                            InputField(
                                label = "Confirmar Contraseña",
                                state = confirmPasswordState,
                                isPassword = true,
                                icon = R.drawable.lock_icon
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = aceptoTerminos,
                            onCheckedChange = { aceptoTerminos = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Acepto los Términos de uso y la Política de Privacidad",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = esRepresentante,
                            onCheckedChange = { esRepresentante = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(
                            text = "Confirmo que represento a esta empresa u organización.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Crear Cuenta Empresarial",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    state: TextFieldState,
    modifier: Modifier = Modifier,
    icon: Any? = null,
    isPassword: Boolean = false
) {
    val leadingIconContent: @Composable (() -> Unit)? = icon?.let {
        {
            when (it) {
                is Int -> Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                is ImageVector -> Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                else -> {}
            }
        }
    }
    
    if (isPassword) {
        OutlinedSecureTextField(
            state = state,
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
            label = { Text(text = label) },
            leadingIcon = leadingIconContent,
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    } else {
        OutlinedTextField(
            state = state,
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
            label = { Text(text = label) },
            leadingIcon = leadingIconContent,
            shape = RoundedCornerShape(4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun CreateBussinesAccountPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        CreateBussinesAccount()
    }
}
