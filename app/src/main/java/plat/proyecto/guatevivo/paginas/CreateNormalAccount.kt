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
fun CreateNormalAccount(
    modifier: Modifier = Modifier
) {
    val nombreState = rememberTextFieldState()
    val apellidoState = rememberTextFieldState()
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()
    var aceptoTerminos by remember { mutableStateOf(false) }

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
                    text = "¡Únete a la aventura!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regístrate para descubrir y crear los mejores eventos en Guatemala.",
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
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .clickable { /* Seleccionar foto */ },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.account_circle),
                                    contentDescription = "Seleccionar foto de perfil",
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "Añadir foto",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InputField(
                                    modifier = Modifier.weight(1f),
                                    label = "Nombre",
                                    state = nombreState
                                )
                                InputField(
                                    modifier = Modifier.weight(1f),
                                    label = "Apellido",
                                    state = apellidoState
                                )
                            }

                            InputField(
                                label = "Correo Electrónico",
                                state = emailState
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = { aceptoTerminos = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Acepto los Términos de uso y la Política de Privacidad",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
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
                    text = "Crear Cuenta",
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
fun CreateNormalAccountPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        CreateNormalAccount()
    }
}
