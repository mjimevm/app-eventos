package plat.proyecto.guatevivo.paginas.register.ui.business

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.core.bars.TopBar
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable
data object RegisterBusinessDestination

fun NavController.navigateToRegisterBusiness(navOptions: NavOptions? = null) {
    navigate(RegisterBusinessDestination, navOptions)
}

fun NavGraphBuilder.registerBusinessScreen(onCreate: () -> Unit,
                                           onBack: () -> Unit) {
    composable<RegisterBusinessDestination> {
        CreateBusinessRoute(
            onCreate = onCreate,
            onBack = onBack)
    }
}

@Composable
fun CreateBusinessRoute(
    onCreate: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterBusinessViewModel = viewModel(factory = RegisterBusinessViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onCreate()
        }
    }

    CreateBussinesAccount(
        state = state,
        onEvent = viewModel::onEvent,
        onCreate = { viewModel.onEvent(RegisterBusinessEvent.OnRegisterClicked) },
        onBack = onBack,
        demoContent = { if (!state.isLoading && !state.isSuccess) BusinessDemoEntry() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBussinesAccount(
    modifier: Modifier = Modifier,
    state: RegisterBusinessState = RegisterBusinessState(),
    onEvent: (RegisterBusinessEvent) -> Unit = {},
    onCreate: () -> Unit = {},
    onBack: () -> Unit = {},
    demoContent: @Composable () -> Unit = {}
) {
    val nombreEmpresaState = rememberTextFieldState(initialText = state.empresaNombre)
    val emailEmpresarialState = rememberTextFieldState(initialText = state.correo)
    val telefonoState = rememberTextFieldState(initialText = state.telefono)
    val direccionState = rememberTextFieldState(initialText = state.direccion)
    val descripcionState = rememberTextFieldState(initialText = state.descripcion)
    val passwordState = rememberTextFieldState(initialText = state.password)
    val confirmPasswordState = rememberTextFieldState(initialText = state.confirmPassword)
    var aceptoTerminos by rememberSaveable { mutableStateOf(state.aceptoTerminos) }
    var esRepresentante by rememberSaveable { mutableStateOf(state.esRepresentante) }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val canEdit = !state.isLoading && state.partialUid == null && !state.isSuccess

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBar(showBackButton = true, onBackClick = onBack)
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
                if (state.partialUid != null) {
                    Text("Authentication ya creó la cuenta. El botón reintenta guardar el perfil original; no vuelve a registrar el correo.")
                }
                demoContent()

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

                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Atención",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

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
                                state = nombreEmpresaState,
                                enabled = canEdit
                            )

                            InputField(
                                label = "Correo Empresarial",
                                state = emailEmpresarialState,
                                enabled = canEdit
                            )

                            InputField(
                                label = "Teléfono de Contacto",
                                state = telefonoState,
                                enabled = canEdit
                            )

                            InputField(
                                label = "Dirección",
                                state = direccionState,
                                enabled = canEdit
                            )

                            InputField(
                                label = "Descripción",
                                state = descripcionState,
                                enabled = canEdit
                            )

                            InputField(
                                label = "Contraseña",
                                state = passwordState,
                                enabled = canEdit,
                                isPassword = true,
                                icon = R.drawable.lock_icon
                            )

                            InputField(
                                label = "Confirmar Contraseña",
                                state = confirmPasswordState,
                                enabled = canEdit,
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
                            enabled = canEdit,
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
                            enabled = canEdit,
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
                onClick = {
                    onEvent(RegisterBusinessEvent.OnEmpresaChanged(nombreEmpresaState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnCorreoChanged(emailEmpresarialState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnTelefonoChanged(telefonoState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnDireccionChanged(direccionState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnDescripcionChanged(descripcionState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnPasswordChanged(passwordState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnConfirmPasswordChanged(confirmPasswordState.text.toString()))
                    onEvent(RegisterBusinessEvent.OnAceptoTerminosChanged(aceptoTerminos))
                    onEvent(RegisterBusinessEvent.OnRepresentanteChanged(esRepresentante))
                    onCreate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                enabled = !state.isLoading && !state.isSuccess
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (state.partialUid != null) "Reintentar guardar perfil" else "Crear Cuenta Empresarial",
                        fontSize = 18.sp,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
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
    isPassword: Boolean = false,
    enabled: Boolean = true
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
            enabled = enabled,
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
            enabled = enabled,
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
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CreateBussinesAccountPreview() {
    GuatevivoTheme {
        CreateBussinesAccount()
    }
}
