package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.FriendDto
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.FriendRequestDto

interface FriendsFirebase {
    suspend fun sendFriendRequest(email: String)
    suspend fun getPendingRequests(): List<FriendRequestDto>
    suspend fun acceptFriendRequest(requestId: String)
    suspend fun rejectFriendRequest(requestId: String)
    suspend fun getFriends(): List<FriendDto>
    suspend fun deleteFriend(friendUid: String)
}