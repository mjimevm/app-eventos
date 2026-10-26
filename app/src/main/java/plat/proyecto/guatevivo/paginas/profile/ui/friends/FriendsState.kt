package plat.proyecto.guatevivo.paginas.profile.ui.friends

import plat.proyecto.guatevivo.R

data class FriendItem(
    val id: Int,
    val nombre: String,
    val ubicacion: String,
    val eventoComun: String,
    val avatarRes: Int
)

data class FriendsState(
    val amigos: List<FriendItem> = listOf(
        FriendItem(1, "Carlos Morales", "Antigua Guatemala", "Festival de las Flores", R.drawable.account_circle),
        FriendItem(2, "Ana Sofía Gómez", "Ciudad de Guatemala", "Concierto Filarmónica", R.drawable.account_circle),
        FriendItem(3, "Diego Mendoza", "Panajachel, Sololá", "Cena de Café en Atitlán", R.drawable.account_circle),
        FriendItem(4, "Lucía Ramírez", "Quetzaltenango", "Feria de Independencia", R.drawable.account_circle)
    ),
    val searchQuery: String = "",
    val isLoading: Boolean = false
)
