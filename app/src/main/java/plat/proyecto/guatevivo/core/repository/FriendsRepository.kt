package plat.proyecto.guatevivo.core.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.FriendsFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType

data class FriendsOverview(
    val uid: String,
    val ownProfile: PublicProfileDto,
    val relationships: List<FriendRequestDto>,
    val profiles: Map<String, PublicProfileDto>
)

class FriendsRepository(
    private val firebase: FriendsFirebase,
    private val currentUserId: () -> String?,
    private val accountType: suspend () -> String? = { null }
) {
    fun currentUid(): String = currentUserId()?.takeIf { it.isNotBlank() } ?: error("Debes iniciar sesión")

    private suspend fun requireUserAccount() {
        val type = accountType()
        if (type != null) {
            require(type == AccountType.USER.name) { "Las cuentas BUSINESS no pueden usar Amigos" }
        }
    }

    suspend fun load(): FriendsOverview {
        requireUserAccount()
        val uid = currentUid()
        val own = try {
            firebase.ensureOwnProfile()
        } catch (e: Exception) {
            throw FriendsActionException(friendsErrorCode(e), "PERFILES (users/publicProfiles): ${e.localizedMessage ?: "no se pudo sincronizar"}", e)
        }
        val relationships = try {
            firebase.getRelationships()
        } catch (e: Exception) {
            throw FriendsActionException(friendsErrorCode(e), "friendRequests: ${e.localizedMessage ?: "no se pudo consultar"}", e)
        }.filter {
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
        requireUserAccount()
        val actor = currentUid()
        val profile = firebase.getProfile(uid)?.takeUnless { it.isDeleted }
        check(currentUid() == actor) { "La sesión cambió" }
        return profile
    }
    suspend fun send(email: String): EmailFriendRequestOutcome {
        requireUserAccount()
        val actor = currentUid()
        val normalized = normalizeFriendEmail(email)
        require(isValidFriendEmail(normalized)) { "Introduce un correo electrónico válido" }
        val outcome = firebase.sendFriendRequest(normalized)
        check(currentUid() == actor) { "La sesión cambió" }
        return outcome
    }
    suspend fun accept(id: String) { requireUserAccount(); currentUid(); firebase.acceptFriendRequest(id) }
    suspend fun reject(id: String) { requireUserAccount(); currentUid(); firebase.rejectFriendRequest(id) }
    suspend fun cancel(id: String) { requireUserAccount(); currentUid(); firebase.cancelFriendRequest(id) }
    suspend fun remove(uid: String) {
        requireUserAccount()
        require(uid != currentUid() && uid.isNotBlank()) { "Amistad no válida" }
        firebase.deleteFriend(uid)
    }
}
