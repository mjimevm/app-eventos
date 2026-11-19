package plat.proyecto.guatevivo.paginas.event.ui.create

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.core.bars.BottomNavigationBar
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Serializable
data class CreateEventDestination(val eventId: String? = null)

fun NavController.navigateToCreateEvent(navOptions: NavOptions? = null) {
    navigate(CreateEventDestination(), navOptions)
}

fun NavController.navigateToEditEvent(eventId: String) {
    navigate(CreateEventDestination(eventId)) { launchSingleTop = true }
}

fun NavGraphBuilder.createEventScreen(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onBackClick: () -> Unit = {}
) {
    composable<CreateEventDestination> { entry ->
        CreateRoute(
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
            onCreateClick = onCreateClick,
            onProfileClick = onProfileClick,
            eventId = entry.toRoute<CreateEventDestination>().eventId,
            onBackClick = onBackClick
        )
    }
}

@Composable
fun CreateRoute(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    eventId: String? = null,
    onBackClick: () -> Unit = {}
) {
    val context = LocalContext.current.applicationContext
    val app = context as GuateVivoApp

    val factory = remember(app, eventId) {
        CreateEventViewModelFactory(app.eventRepository, eventId, app.authRepository::getCurrentUserId)
    }

    val viewModel: CreateEventViewModel = viewModel(
        factory = factory
    )

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess && state.isEditing) {
            viewModel.onEvent(CreateEventEvent.ClearSuccess)
            onBackClick()
        }
    }

    CreateEventScreen(
        state = state,
        onEvent = viewModel::onEvent,
        onHomeClick = onHomeClick,
        onSearchClick = onSearchClick,
        onCreateClick = onCreateClick,
        onProfileClick = onProfileClick,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventScreen(
    state: CreateEventState = CreateEventState(),
    onEvent: (CreateEventEvent) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            onEvent(CreateEventEvent.UpdateImageUri(uri.toString()))
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(state.fecha) {
            if (state.fecha.isNotBlank()) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    val localDate = LocalDate.parse(state.fecha.trim(), formatter)
                    localDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
                } catch (e: Exception) {
                    null
                }
            } else null
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                        val formattedDate = localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        onEvent(CreateEventEvent.UpdateFecha(formattedDate))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val initialTime = remember(state.hora) {
            if (state.hora.isNotBlank()) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("HH:mm")
                    LocalTime.parse(state.hora.trim(), formatter)
                } catch (e: Exception) {
                    null
                }
            } else null
        }
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime?.hour ?: 12,
            initialMinute = initialTime?.minute ?: 0,
            is24Hour = true
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onEvent(CreateEventEvent.UpdateHora(formattedTime))
                    showTimePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (state.isSuccess && !state.isEditing) {
        AlertDialog(
            onDismissRequest = {
                onEvent(CreateEventEvent.ClearSuccess)
            },
            title = {
                Text("¡Borrador guardado!")
            },
            text = {
                Text(
                    "Tu evento se guardó correctamente. " +
                            "Podrás editarlo y publicarlo después."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onEvent(CreateEventEvent.ClearSuccess)
                        onHomeClick()
                    }
                ) {
                    Text("Continuar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopBar(showBackButton = state.isEditing, onBackClick = onBackClick)
        },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = 2,
                onItemClick = { index ->
                    when (index) {
                        0 -> onHomeClick()
                        1 -> onSearchClick()
                        2 -> onCreateClick()
                        3 -> onProfileClick()
                    }
                }
            )
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
                    text = if (state.isEditing) "Editar evento" else "Crea tu evento",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Diseña algo único para tu evento especial",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                state.error?.let { errorMessage ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { onEvent(CreateEventEvent.ClearError) }) {
                                Text("Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
                            }
                        }
                    }
                }
                if (state.isEditing && !state.canSave && !state.isLoading) {
                    TextButton(onClick = { onEvent(CreateEventEvent.RetryLoad) }) { Text("Reintentar carga") }
                }

                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        // Image Header Selector Box
                        if (!state.imageUri.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = state.imageUri,
                                    contentDescription = "Imagen del evento",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        enabled = !state.isLoading && state.canSave,
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            contentColor = MaterialTheme.colorScheme.onSurface
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Cambiar", fontSize = 12.sp)
                                    }
                                    Button(
                                        enabled = !state.isLoading && state.canSave,
                                        onClick = { onEvent(CreateEventEvent.UpdateImageUri(null)) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text("Quitar", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .background(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                    .clickable(enabled = !state.isLoading && state.canSave) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.outline_animated_images_24),
                                        contentDescription = "Subir Imagen",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "+ Agregar imagen opcional",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Toca para seleccionar de tu dispositivo",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            InputField(
                                enabled = !state.isLoading && state.canSave,
                                label = "Nombre del Evento",
                                value = state.nombreEvento,
                                onValueChange = { onEvent(CreateEventEvent.UpdateNombreEvento(it)) },
                                placeholder = "Ej. Cena familiar",
                                icon = null
                            )

                            InputField(
                                enabled = !state.isLoading && state.canSave,
                                label = "Ubicación",
                                value = state.ubicacion,
                                onValueChange = { onEvent(CreateEventEvent.UpdateUbicacion(it)) },
                                placeholder = "Ej. Ciudad de Guatemala",
                                icon = R.drawable.location_icon
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InputField(
                                    enabled = !state.isLoading && state.canSave,
                                    modifier = Modifier.weight(1f),
                                    label = "Fecha",
                                    value = state.fecha,
                                    onValueChange = { onEvent(CreateEventEvent.UpdateFecha(it)) },
                                    placeholder = "dd/MM/yyyy",
                                    icon = R.drawable.calendar_icon,
                                    onClick = { showDatePicker = true }
                                )
                                InputField(
                                    enabled = !state.isLoading && state.canSave,
                                    modifier = Modifier.weight(1f),
                                    label = "Hora",
                                    value = state.hora,
                                    onValueChange = { onEvent(CreateEventEvent.UpdateHora(it)) },
                                    placeholder = "HH:mm",
                                    icon = R.drawable.hour_icon,
                                    onClick = { showTimePicker = true }
                                )
                            }

                            InputField(
                                enabled = !state.isLoading && state.canSave,
                                label = "Acerca del Evento",
                                value = state.descripcion,
                                onValueChange = { onEvent(CreateEventEvent.UpdateDescripcion(it)) },
                                placeholder = "Escribe una breve descripción",
                                singleLine = false,
                                minHeight = 100.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        enabled = !state.isLoading && state.canSave,
                        checked = state.incluirConfirmacion,
                        onCheckedChange = { onEvent(CreateEventEvent.UpdateIncluirConfirmacion(it)) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Incluir apartado de confirmación",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Invitation Preview Card
                InvitationPreviewCard(
                    nombreEvento = state.nombreEvento,
                    fecha = state.fecha,
                    hora = state.hora,
                    ubicacion = state.ubicacion,
                    descripcion = state.descripcion,
                    imageUri = state.imageUri
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            Button(
                onClick = { onEvent(CreateEventEvent.CreateEvent) },
                enabled = !state.isLoading && state.canSave && !state.isSuccess,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (state.isEditing) "Guardar cambios" else "Generar invitación",
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
private fun InvitationPreviewCard(
    nombreEvento: String,
    fecha: String,
    hora: String,
    ubicacion: String,
    descripcion: String,
    imageUri: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Vista previa de la invitación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column {
                // Caso B: Si hay imagen, la mostramos arriba. Caso A: Si NO hay imagen, NO mostramos ningún Box o placeholder.
                if (!imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Imagen de invitación",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Nombre del evento
                    Text(
                        text = "NOMBRE DEL EVENTO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = nombreEvento.ifBlank { "Cena familiar" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fecha y Hora ordenadamente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "FECHA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = fecha.ifBlank { "21/02/2027" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "HORA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hora.ifBlank { "11:22" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Ubicación
                    Text(
                        text = "UBICACIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = ubicacion.ifBlank { "Ciudad de Guatemala" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Descripción
                    Text(
                        text = "DESCRIPCIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = descripcion.ifBlank { "Acompáñanos a celebrar una ocasión especial." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    )
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: Int? = null,
    singleLine: Boolean = true,
    minHeight: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                enabled = enabled,
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
                    .clip(shape = RoundedCornerShape(4.dp)),
                placeholder = { Text(text = placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                shape = RoundedCornerShape(4.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                ),
                trailingIcon = icon?.let {
                    {
                        IconButton(enabled = enabled, onClick = { onClick?.invoke() }) {
                            Icon(
                                painter = painterResource(id = it),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = singleLine,
                readOnly = onClick != null
            )
            if (onClick != null) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(enabled = enabled) { onClick() }
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CreateEventPreview() {
    GuatevivoTheme {
        CreateEventScreen()
    }
}
