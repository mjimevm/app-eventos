package plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase

import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.FriendRequestDto
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.EmailFriendRequestOutcome
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.PublicProfileDto

interface FriendsFirebase {
    suspend fun ensureOwnProfile(): PublicProfileDto
    suspend fun getProfile(uid: String): PublicProfileDto?
    suspend fun sendFriendRequest(email: String): EmailFriendRequestOutcome
    suspend fun getRelationships(): List<FriendRequestDto>
    suspend fun acceptFriendRequest(requestId: String)
    suspend fun rejectFriendRequest(requestId: String)
    suspend fun cancelFriendRequest(requestId: String)
    suspend fun deleteFriend(friendUid: String)
}
