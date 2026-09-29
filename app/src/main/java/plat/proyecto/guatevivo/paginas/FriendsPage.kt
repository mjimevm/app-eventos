package plat.proyecto.guatevivo.paginas

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

data class Friend(
    val id: String,
    val name: String,
    val email: String,
    val imageResId: Int? = null
)

@Composable
fun FriendsScreen(
    friends: List<Friend>,
    modifier: Modifier = Modifier,
    friendRequests: List<Friend> = emptyList(),
    initialTab: Int = 0,
    onBackClick: () -> Unit = {},
    onSendFriendRequest: (String) -> Unit = {},
    onDeleteFriend: (Friend) -> Unit = {},
    onAcceptRequest: (Friend) -> Unit = {},
    onDeclineRequest: (Friend) -> Unit = {}
) {
    val emailState = rememberTextFieldState()
    var selectedTab by remember { mutableIntStateOf(initialTab) }

    Scaffold(
        topBar = {
            TopBar(true)
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = -1)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Amigos",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                thickness = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Agregar amigos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            SearchBarSection("Buscar amigos por correo electrónico")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val email = emailState.text.toString().trim()
                    if (email.isNotEmpty()) {
                        onSendFriendRequest(email)
                        emailState.clearText()
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Enviar solicitud",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Mis amigos (${friends.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Solicitudes (${friendRequests.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                if (friends.isEmpty()) {
                    Text(
                        text = "Todavía no tienes amigos agregados.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = friends, key = { friend -> friend.id }) { friend ->
                            FriendItem(
                                friend = friend,
                                onDeleteFriend = { onDeleteFriend(friend) }
                            )
                        }
                    }
                }
            } else {
                if (friendRequests.isEmpty()) {
                    Text(
                        text = "No tienes solicitudes de amistad pendientes.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items = friendRequests, key = { req -> req.id }) { request ->
                            FriendRequestItem(
                                friend = request,
                                onAccept = { onAcceptRequest(request) },
                                onDecline = { onDeclineRequest(request) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onDeleteFriend: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        Surface(
            onClick = { menu = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (friend.imageResId == null) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = friend.name.firstOrNull()?.uppercase() ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Icon(
                        painter = painterResource(id = friend.imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = menu,
            onDismissRequest = { menu = false }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Eliminar amigo",
                        color = MaterialTheme.colorScheme.error
                    )
                },
                onClick = {
                    menu = false
                    onDeleteFriend()
                }
            )
        }
    }
}

@Composable
fun FriendRequestItem(
    friend: Friend,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (friend.imageResId == null) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = friend.name.firstOrNull()?.uppercase() ?: "",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                } else {
                    Icon(
                        painter = painterResource(id = friend.imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = friend.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(text = "Aceptar")
                }

                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "Rechazar",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FriendsScreenPreview() {
    GuatevivoTheme {
        val testFriends = listOf(
            Friend(
                id = "1",
                name = "Alejandro",
                email = "Alejandro@gmail.com"
            ),
            Friend(
                id = "2",
                name = "Jimena",
                email = "maria@gmail.com"
            )
        )

        val testRequests = listOf(
            Friend(
                id = "3",
                name = "Carlos Gomez",
                email = "carlos@gmail.com"
            )
        )

        FriendsScreen(
            friends = testFriends,
            friendRequests = testRequests
        )
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FriendsScreenSolicitudesPreview() {
    GuatevivoTheme {
        val testFriends = listOf(
            Friend(
                id = "1",
                name = "Alejandro",
                email = "Alejandro@gmail.com"
            )
        )

        val testRequests = listOf(
            Friend(
                id = "3",
                name = "Carlos Gomez",
                email = "carlos@gmail.com"
            ),
            Friend(
                id = "4",
                name = "Sofia Martinez",
                email = "sofia@gmail.com"
            )
        )

        FriendsScreen(
            friends = testFriends,
            friendRequests = testRequests,
            initialTab = 1
        )
    }
}