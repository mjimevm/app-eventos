package plat.proyecto.guatevivo.paginas

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen() {
    var nombreEvento by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var incluirConfirmacion by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopBar(showBackButton = false)
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = 2)
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
                    text = "Crea tu evento",
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_animated_images_24),
                                contentDescription = "Subir Imagen",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            InputField (
                                label = "Nombre del Evento",
                                value = nombreEvento,
                                onValueChange = { nombreEvento = it },
                                placeholder = "Ej. Cena familiar",
                                icon = null
                            )

                            InputField(
                                label = "Ubicación",
                                value = ubicacion,
                                onValueChange = { ubicacion = it },
                                placeholder = "Ej. Ciudad de Guatemala",
                                icon = R.drawable.location_icon
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                InputField(
                                    modifier = Modifier.weight(1f),
                                    label = "Fecha",
                                    value = fecha,
                                    onValueChange = { fecha = it },
                                    placeholder = "dd / mm / aaaa",
                                    icon = R.drawable.calendar_icon
                                )
                                InputField(
                                    modifier = Modifier.weight(1f),
                                    label = "Hora",
                                    value = hora,
                                    onValueChange = { hora = it },
                                    placeholder = "--:--",
                                    icon = R.drawable.hour_icon
                                )
                            }

                            InputField(
                                label = "Acerca del Evento",
                                value = descripcion,
                                onValueChange = { descripcion = it },
                                placeholder = "Escribe una breve descripción",
                                singleLine = false,
                                minHeight = 100.dp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Checkbox(
                        checked = incluirConfirmacion,
                        onCheckedChange = { incluirConfirmacion = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Incluir apartado de confirmación",
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
                    text = "Generar invitación",
                    fontSize = 18.sp,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
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
    minHeight: androidx.compose.ui.unit.Dp = 0.dp
) {
    Column(modifier = modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight),
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
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            singleLine = singleLine
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateEventPreview() {
    plat.proyecto.guatevivo.ui.theme.GuatevivoTheme {
        CreateEventScreen()
    }
}