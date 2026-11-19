package plat.proyecto.guatevivo.paginas.profile.ui.friends

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import coil3.compose.AsyncImage
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.core.bars.BottomNavigationBar
import plat.proyecto.guatevivo.core.bars.TopBar
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.PublicProfileDto
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.EmailFriendRequestOutcome
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

@Serializable data object FriendsDestination
@Serializable data class PublicProfileDestination(val uid: String)

fun NavController.navigateToFriends(navOptions: NavOptions? = null) { navigate(FriendsDestination, navOptions) }
fun NavController.navigateToPublicProfile(uid: String) { navigate(PublicProfileDestination(uid)) { launchSingleTop = true } }

fun NavGraphBuilder.friendsScreen(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenProfile: (String) -> Unit
) {
    composable<FriendsDestination> {
        FriendsRoute(onBackClick, onHomeClick, onSearchClick, onCreateClick, onProfileClick, onOpenProfile)
    }
    composable<PublicProfileDestination> { entry ->
        val uid = entry.toRoute<PublicProfileDestination>().uid
        val viewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.factory(uid))
        val state by viewModel.state.collectAsStateWithLifecycle()
        LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
        FriendsScaffold(onBackClick, onHomeClick, onSearchClick, onCreateClick, onProfileClick) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Text("Perfil", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                    TextButton(onClick = viewModel::refresh) { Text("Reintentar") }
                }
                state.openedProfile?.let { profile -> FriendItem(profile, onOpenProfile = {}) {} }
            }
        }
    }
}

@Composable
fun FriendsRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenProfile: (String) -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = FriendsViewModel.factory())
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    FriendsScreen(state, viewModel::onEvent, onBackClick, onHomeClick, onSearchClick, onCreateClick, onProfileClick, onOpenProfile)
}

@Composable
private fun FriendsScaffold(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onCreateClick: () -> Unit,
    onProfileClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = { TopBar(true, onBackClick = onBackClick) },
        bottomBar = {
            BottomNavigationBar(selectedItem = -1, onItemClick = {
                when (it) { 0 -> onHomeClick(); 1 -> onSearchClick(); 2 -> onCreateClick(); 3 -> onProfileClick() }
            })
        },
        containerColor = MaterialTheme.colorScheme.surface, content = content
    )
}

@Composable
private fun FriendsScreen(
    state: FriendsState = FriendsState(),
    onEvent: (FriendsEvent) -> Unit = {},
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onCreateClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onOpenProfile: (String) -> Unit = {}
) {
    val enabled = !state.isLoading && !state.isSending && state.processingUid == null && state.uid.isNotBlank()
    if (state.removeUid != null) AlertDialog(
        onDismissRequest = { onEvent(FriendsEvent.CancelRemove) },
        title = { Text("Eliminar amigo") },
        text = {
            Column {
                Text("¿Eliminar esta amistad? Podrán volver a enviarse una solicitud.")
                if (state.processingUid != null) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(enabled = enabled, onClick = { onEvent(FriendsEvent.ConfirmRemove) }) { Text("Eliminar") } },
        dismissButton = { TextButton(enabled = enabled, onClick = { onEvent(FriendsEvent.CancelRemove) }) { Text("Cancelar") } }
    )
    FriendsScaffold(onBackClick, onHomeClick, onSearchClick, onCreateClick, onProfileClick) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("Amigos", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
                HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), thickness = 3.dp, color = MaterialTheme.colorScheme.primary)
                if (state.isLoading || state.isSending || state.processingUid != null) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                    TextButton(onClick = { onEvent(FriendsEvent.ClearError) }) { Text("Cerrar") }
                }
                TextButton(enabled = !state.isLoading && !state.isSending && state.processingUid == null, onClick = { onEvent(FriendsEvent.Refresh) }) { Text("Actualizar") }
                Text("Agregar amigos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onEvent(FriendsEvent.UpdateEmail(it)) },
                    label = { Text("Correo electrónico") },
                    supportingText = { Text("Correo principal de una cuenta registrada") },
                    singleLine = true,
                    enabled = !state.isSending,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Button(
                    enabled = !state.isLoading && !state.isSending && state.processingUid == null && state.email.isNotBlank(),
                    onClick = { onEvent(FriendsEvent.SendRequest) },
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(55.dp)
                ) { Text(if (state.isSending) "Enviando…" else "Enviar solicitud") }
                state.sendError?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp)) }
                state.sendOutcome?.let { outcome ->
                    Text(
                        when (outcome) {
                            EmailFriendRequestOutcome.SENT -> "Solicitud enviada"
                            EmailFriendRequestOutcome.PENDING -> "Ya existe una solicitud pendiente entre estas cuentas"
                            EmailFriendRequestOutcome.FRIENDS -> "Esta cuenta ya está en tus amigos"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            item {
                PrimaryTabRow(selectedTabIndex = state.selectedTab, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    listOf("Mis amigos (${state.friends.size})", "Solicitudes (${state.requests.size})").forEachIndexed { index, title ->
                        Tab(selected = state.selectedTab == index, onClick = { onEvent(FriendsEvent.SelectTab(index)) }, text = { Text(title) })
                    }
                }
            }
            val entries = if (state.selectedTab == 0) state.friends else state.requests
            if (entries.isEmpty() && !state.isLoading && state.error == null) item {
                EmptyFriendsText(if (state.selectedTab == 0) "Todavía no tienes amigos agregados." else "No tienes solicitudes pendientes.")
            }
            items(entries, key = { "relation-${it.id}" }) { request ->
                val uid = state.otherUid(request)
                state.profiles[uid]?.let { profile ->
                    FriendRequestItem(profile, { onOpenProfile(uid) }) { FriendActions(uid, state, enabled, onEvent, onOpenProfile) }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RowScope.FriendActions(uid: String, state: FriendsState, enabled: Boolean, onEvent: (FriendsEvent) -> Unit, onOpenProfile: (String) -> Unit) {
    val request = state.relationship(uid)
    when {
        request == null -> Text("Sin solicitud activa", style = MaterialTheme.typography.bodySmall)
        request.status == "ACCEPTED" -> {
            var menu by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(enabled = enabled, onClick = { menu = true }, shape = RoundedCornerShape(4.dp)) { Text("Amigos") }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Ver perfil") }, onClick = { menu = false; onOpenProfile(uid) })
                    DropdownMenuItem(text = { Text("Eliminar amigo", color = MaterialTheme.colorScheme.error) }, onClick = { menu = false; onEvent(FriendsEvent.Remove(uid)) })
                }
            }
        }
        request.senderUid == state.uid -> {
            Text("Solicitud enviada", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            TextButton(enabled = enabled, onClick = { onEvent(FriendsEvent.Cancel(request.id)) }) { Text("Cancelar") }
        }
        else -> {
            Button(enabled = enabled, onClick = { onEvent(FriendsEvent.Accept(request.id)) }, shape = RoundedCornerShape(4.dp)) { Text("Aceptar") }
            OutlinedButton(enabled = enabled, onClick = { onEvent(FriendsEvent.Reject(request.id)) }, shape = RoundedCornerShape(4.dp)) { Text("Rechazar") }
        }
    }
}

@Composable
fun FriendItem(profile: PublicProfileDto, onOpenProfile: () -> Unit, actions: @Composable RowScope.() -> Unit) {
    Surface(onClick = onOpenProfile, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, tonalElevation = 2.dp, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FriendAvatar(profile)
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(profile.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(if (profile.accountType == "BUSINESS") "Empresa" else "Cuenta personal", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End), verticalAlignment = Alignment.CenterVertically, content = actions)
        }
    }
}

@Composable
fun FriendRequestItem(profile: PublicProfileDto, onOpenProfile: () -> Unit, actions: @Composable RowScope.() -> Unit) = FriendItem(profile, onOpenProfile, actions)

@Composable
private fun FriendAvatar(profile: PublicProfileDto) {
    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        if (profile.photoUrl.startsWith("https://")) AsyncImage(
            model = profile.photoUrl, contentDescription = "Foto de ${profile.name}",
            contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape)
        ) else Box(contentAlignment = Alignment.Center) {
            Text(profile.name.firstOrNull()?.uppercase().orEmpty(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable private fun EmptyFriendsText(text: String) {
    Text(text, modifier = Modifier.fillMaxWidth().padding(16.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable fun FriendsScreenPreview() { GuatevivoTheme { FriendsScreen() } }
