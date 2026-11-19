package plat.proyecto.guatevivo.testing

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.FriendsFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

fun visibleProfile(uid: String, name: String = uid) = PublicProfileDto(
    uid = uid, name = name, nameNormalized = normalizeProfileName(name), isDiscoverable = false
)

class FriendStore {
    val profiles = mutableMapOf<String, PublicProfileDto>()
    val users = mutableMapOf<String, UserDto>()
    val emails = mutableMapOf<String, String>()
    val relationships = mutableMapOf<String, FriendRequestDto>()
    val mutex = Mutex()
    fun request(sender: String, receiver: String, status: String = "PENDING"): FriendRequestDto {
        val value = FriendRequestDto(id = friendRequestId(sender, receiver), senderUid = sender, receiverUid = receiver, status = status)
        relationships[value.id] = value
        return value
    }
}

class FakeFriendsFirebase(val store: FriendStore = FriendStore(), var actor: String = "me") : FriendsFirebase {
    var failure: Exception? = null
    var mutationFailure: Exception? = null
    var readFailureAfterSend: Exception? = null
    var mutationGate: CompletableDeferred<Unit>? = null
    val sentEmails = mutableListOf<String>()
    val profileReads = mutableListOf<String>()
    val sendCalls get() = sentEmails.size
    var removeCalls = 0

    override suspend fun ensureOwnProfile(): PublicProfileDto {
        failure?.let { throw it }
        val profile = store.users[actor]?.copy(uid = actor)?.publicProfile() ?: store.profiles[actor] ?: error("Perfil no encontrado")
        require(!profile.isDeleted)
        store.profiles[actor] = profile
        return profile
    }
    override suspend fun getProfile(uid: String): PublicProfileDto? {
        failure?.let { throw it }
        profileReads.add(uid)
        val profile = store.profiles[uid]?.takeUnless { it.isDeleted } ?: return null
        val related = store.relationships.values.any {
            it.status in listOf("PENDING", "ACCEPTED") &&
                (it.senderUid == actor && it.receiverUid == uid || it.receiverUid == actor && it.senderUid == uid)
        }
        check(uid == actor || related) { "PERMISSION_DENIED: Perfil privado" }
        return profile
    }
    override suspend fun getRelationships(): List<FriendRequestDto> {
        failure?.let { throw it }
        return store.relationships.values.filter {
            (it.senderUid == actor || it.receiverUid == actor) && it.status in listOf("PENDING", "ACCEPTED")
        }
    }
    private suspend fun beforeMutation() { mutationGate?.await(); mutationFailure?.let { throw it } }
    override suspend fun sendFriendRequest(email: String): EmailFriendRequestOutcome {
        val uid = actor
        sentEmails.add(email)
        beforeMutation()
        val result = store.mutex.withLock {
            val other = store.emails[normalizeFriendEmail(email)]
                ?: throw FriendsActionException("NOT_FOUND", "No se pudo enviar la solicitud a esta cuenta.")
            require(other != uid) { "No puedes enviarte una solicitud a ti mismo" }
            val target = store.profiles[other]
            if (target == null || target.isDeleted) throw FriendsActionException("NOT_FOUND", "No se pudo enviar la solicitud a esta cuenta.")
            val existing = listOfNotNull(store.relationships[friendRequestId(uid, other)], store.relationships[friendRequestId(other, uid)])
            when {
                existing.any { it.status == "ACCEPTED" } -> EmailFriendRequestOutcome.FRIENDS
                existing.any { it.status == "PENDING" } -> EmailFriendRequestOutcome.PENDING
                else -> { store.request(uid, other); EmailFriendRequestOutcome.SENT }
            }
        }
        readFailureAfterSend?.let { failure = it }
        return result
    }
    override suspend fun acceptFriendRequest(requestId: String) = respond(requestId, "ACCEPTED")
    override suspend fun rejectFriendRequest(requestId: String) = respond(requestId, "REJECTED")
    private suspend fun respond(id: String, status: String) {
        val uid = actor
        beforeMutation()
        store.mutex.withLock {
            val request = store.relationships[id] ?: error("Solicitud no encontrada")
            FriendshipPolicy.canRespond(uid, request)
            require(store.profiles[request.senderUid]?.isDeleted == false && store.profiles[request.receiverUid]?.isDeleted == false)
            store.relationships[id] = request.copy(status = status)
        }
    }
    override suspend fun cancelFriendRequest(requestId: String) {
        val uid = actor
        beforeMutation()
        store.mutex.withLock {
            val request = store.relationships[requestId] ?: error("Solicitud no encontrada")
            FriendshipPolicy.canCancel(uid, request)
            store.relationships.remove(requestId)
        }
    }
    override suspend fun deleteFriend(friendUid: String) {
        val uid = actor
        removeCalls++
        beforeMutation()
        store.mutex.withLock {
            val records = store.relationships.values.filter {
                it.status == "ACCEPTED" && (it.senderUid == uid && it.receiverUid == friendUid || it.receiverUid == uid && it.senderUid == friendUid)
            }
            require(records.isNotEmpty()) { "Esta amistad no existe" }
            records.forEach { store.relationships.remove(it.id) }
        }
    }
}
