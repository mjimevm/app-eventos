package plat.proyecto.guatevivo.paginas

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R
import plat.proyecto.guatevivo.bars.BottomNavigationBar
import plat.proyecto.guatevivo.bars.TopBar
import plat.proyecto.guatevivo.ui.theme.GuatevivoTheme

data class Friend(
    val id: String,
    val name: String,
    val email: String
)

@Composable
fun FriendsScreen(
    friends: List<Friend>,
    onBackClick: () -> Unit,
    onSendFriendRequest: (String) -> Unit,
    onDeleteFriend: (Friend) -> Unit,
    modifier: Modifier = Modifier
) {
    val emailState = rememberTextFieldState()
    Scaffold(
        topBar = {
            TopBar(true)
        },
        bottomBar = {
            BottomNavigationBar(selectedItem = -1)
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(modifier = modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.Start
        ) {
            Text (
                text = "Amigos",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.padding(16.dp)
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
                modifier = modifier.padding(start = 16.dp)
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
                    .padding(start = 15.dp, end = 15.dp,bottom = 15.dp)
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(4.dp)
            ) {

                Text(
                    text = "Enviar solicitud",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Text(
                text = "Mis amigos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = modifier.padding(start = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))



            if (friends.isEmpty()) {
                Text(text = "Todavía no tienes amigos agregados.",
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
                        .weight(1f)
                ) {
                    items(items = friends, key = { friend -> friend.id }
                    ) { friend ->
                        FriendItem(
                            friend = friend,
                            onDeleteFriend = {
                                onDeleteFriend(friend)
                            }
                        )
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
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                onClick = { menu = true },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.account_circle),
                            contentDescription = null,
                            modifier = Modifier.padding(8.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
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

            HorizontalDivider(
                modifier = Modifier.padding(start = 80.dp, end = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        DropdownMenu(
            expanded = menu,
            onDismissRequest = {
                menu = false
            }
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

@Preview(showBackground = true)
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
            ))

        FriendsScreen(
            friends = testFriends,
            onBackClick = {},
            onSendFriendRequest = {},
            onDeleteFriend = {}
        )
    }
}