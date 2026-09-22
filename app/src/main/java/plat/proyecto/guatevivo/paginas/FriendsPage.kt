package plat.proyecto.guatevivo.paginas

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plat.proyecto.guatevivo.R
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

    Column(modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.Start
    ) {
        TopBar(showBackButton = true, onBackClick = onBackClick)

        Text(text = "Amigos",
            style = MaterialTheme.typography.displayMedium,
            modifier = modifier.padding(15.dp)
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 15.dp),
            thickness = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Text(text = "Agregar amigos",
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier.padding(start = 15.dp)
        )

        OutlinedTextField(
            state = emailState,
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            label = { Text("Correo electrónico") },
            shape = RoundedCornerShape(5.dp)
        )

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
            shape = RoundedCornerShape(5.dp)
        ) {

            Text(text = "Enviar solicitud",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Text(text = "Mis amigos",
            style = MaterialTheme.typography.headlineSmall,
            modifier = modifier.padding(start = 15.dp)
        )

        if (friends.isEmpty()) {
            Text(text = "Todavía no tienes amigos agregados.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
        Surface(
            onClick = { menu = true },
            modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(55.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.account_circle),
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(15.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(text = friend.name,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(text = friend.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        DropdownMenu(
            expanded = menu,
            onDismissRequest = {
                menu = false
            }
        ) {

            DropdownMenuItem(
                text = {
                    Text(text = "Eliminar amigo",
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