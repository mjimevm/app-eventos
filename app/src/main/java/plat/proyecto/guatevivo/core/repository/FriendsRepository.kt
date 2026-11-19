package plat.proyecto.guatevivo.core.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.FriendsFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*

data class FriendsOverview(
    val uid: String,
    val ownProfile: PublicProfileDto,
    val relationships: List<FriendRequestDto>,
    val profiles: Map<String, PublicProfileDto>
)

class FriendsRepository(private val firebase: FriendsFirebase, private val currentUserId: () -> String?) {
    fun currentUid(): String = currentUserId()?.takeIf { it.isNotBlank() } ?: error("Debes iniciar sesión")

    suspend fun load(): FriendsOverview {
        val uid = currentUid()
        val own = firebase.ensureOwnProfile()
        val relationships = firebase.getRelationships().filter {
            it.senderUid.isNotBlank() && it.receiverUid.isNotBlank() &&
                (it.senderUid == uid || it.receiverUid == uid) && it.senderUid != it.receiverUid &&
                it.status in listOf("PENDING", "ACCEPTED")
        }.distinctBy { it.id }
        val ids = relationships.map { if (it.senderUid == uid) it.receiverUid else it.senderUid }.distinct()
        val profiles = mutableListOf<PublicProfileDto>()
        for (chunk in ids.chunked(8)) coroutineScope {
            profiles += chunk.map { async { firebase.getProfile(it) } }.awaitAll().filterNotNull().filterNot { it.isDeleted }
        }
        check(currentUid() == uid) { "La sesión cambió. Vuelve a cargar Amigos." }
        return FriendsOverview(uid, own, relationships, profiles.associateBy { it.uid })
    }

    suspend fun getProfile(uid: String): PublicProfileDto? {
        val actor = currentUid()
        val profile = firebase.getProfile(uid)?.takeUnless { it.isDeleted }
        check(currentUid() == actor) { "La sesión cambió" }
        return profile
    }
    suspend fun send(email: String): EmailFriendRequestOutcome {
        val actor = currentUid()
        val normalized = normalizeFriendEmail(email)
        require(isValidFriendEmail(normalized)) { "Introduce un correo electrónico válido" }
        val outcome = firebase.sendFriendRequest(normalized)
        check(currentUid() == actor) { "La sesión cambió" }
        return outcome
    }
    suspend fun accept(id: String) { currentUid(); firebase.acceptFriendRequest(id) }
    suspend fun reject(id: String) { currentUid(); firebase.rejectFriendRequest(id) }
    suspend fun cancel(id: String) { currentUid(); firebase.cancelFriendRequest(id) }
    suspend fun remove(uid: String) {
        require(uid != currentUid() && uid.isNotBlank()) { "Amistad no válida" }
        firebase.deleteFriend(uid)
    }
}
