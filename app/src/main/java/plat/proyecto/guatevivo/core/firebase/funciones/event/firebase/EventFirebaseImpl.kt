package plat.proyecto.guatevivo.core.firebase.funciones.event.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.EventReadException
import plat.proyecto.guatevivo.core.repository.EventReadFailure

class EventFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventFirebase {

    private val events
        get() = firestore.collection(FirebaseCollections.EVENTS)

    private val users
        get() = firestore.collection(FirebaseCollections.USERS)

    private suspend fun <T> readFromServer(block: suspend () -> T): T = try {
        block()
    } catch (e: FirebaseFirestoreException) {
        val reason = when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE,
            FirebaseFirestoreException.Code.DEADLINE_EXCEEDED -> EventReadFailure.CONNECTION
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> EventReadFailure.PERMISSION_DENIED
            FirebaseFirestoreException.Code.UNAUTHENTICATED -> EventReadFailure.UNAUTHENTICATED
            else -> throw e
        }
        throw EventReadException(reason, e)
    }

    private fun currentUid(): String {
        return auth.currentUser?.uid?: throw IllegalStateException("Usuario no autenticado")
    }

    private suspend fun getCurrentAccountType(): String {
        val user = users.document(currentUid())
            .get()
            .await()
            .toObject(UserDto::class.java)
            ?: throw IllegalStateException("Perfil no encontrado")

        return user.accountType
    }

    private suspend fun getOwnedEvent(eventId: String): EventDto {
        val event = getEventById(eventId) ?: throw IllegalArgumentException("El evento no existe")
        require(event.organizadorId == currentUid()) { "No puedes modificar eventos de otros usuarios" }

        return event
    }

    override suspend fun createDraft(event: EventDto): String {
        val uid = currentUid()
        val document = events.document()

        val draft = event.copy(
            id = document.id,
            organizadorId = uid,
            status = EventStatus.DRAFT.name,
            visibility = EventVisibility.PRIVATE.name,
            createdAt = System.currentTimeMillis()
        )
        document.set(draft).await()
        return document.id
    }

    override suspend fun updateDraft(event: EventDto) {
        val existing = getOwnedEvent(event.id)
        require(existing.status == EventStatus.DRAFT.name) { "El evento ya no es un borrador" }

        events.document(event.id)
            .set(
                event.copy(
                    organizadorId = existing.organizadorId,
                    status = existing.status,
                    visibility = existing.visibility,
                    createdAt = existing.createdAt
                )
            ).await()
    }

    override suspend fun publishEvent(eventId: String) {
        val event = getOwnedEvent(eventId)
        val accountType = getCurrentAccountType()

        require(event.status == EventStatus.DRAFT.name) { "Solo puedes publicar borradores" }

        val visibility = when (accountType) {
            AccountType.BUSINESS.name -> EventVisibility.PUBLIC.name
            AccountType.USER.name -> EventVisibility.PRIVATE.name
            else -> throw IllegalStateException("Tipo de cuenta no válido")
        }

        events.document(eventId)
            .update(
                mapOf(
                    "status" to EventStatus.PUBLISHED.name,
                    "visibility" to visibility
                )
            ).await()
    }

    override suspend fun getPublicEvents(): List<EventDto> = readFromServer {
        events
            .whereEqualTo("status", EventStatus.PUBLISHED.name)
            .whereEqualTo("visibility", EventVisibility.PUBLIC.name)
            .get(Source.SERVER)
            .await()
            .documents.mapNotNull { document ->
                document.toObject(EventDto::class.java)?.copy(id = document.id)
            }
    }

    override suspend fun getMyDrafts(): List<EventDto> {
        return events
            .whereEqualTo("organizadorId", currentUid())
            .whereEqualTo("status", EventStatus.DRAFT.name)
            .get()
            .await()
            .toObjects(EventDto::class.java)
    }

    override suspend fun getMyCreatedEvents(): List<EventDto> {
        return getEventsByOrganizer(currentUid())
    }

    override suspend fun getEventById(eventId: String): EventDto? = readFromServer {
        val document = events.document(eventId).get(Source.SERVER).await()
        document.toObject(EventDto::class.java)?.copy(id = document.id)
    }

    override suspend fun getEventsByOrganizer(
        organizerId: String
    ): List<EventDto> {
        return events
            .whereEqualTo("organizadorId", organizerId)
            .get()
            .await()
            .toObjects(EventDto::class.java)
    }

    override suspend fun updateEvent(event:EventDto) {
        val existing = getOwnedEvent(event.id)

        events.document(event.id)
            .set(
                event.copy(
                    organizadorId = existing.organizadorId,
                    status = existing.status,
                    visibility = existing.visibility,
                    createdAt = existing.createdAt
                )
            ).await()
    }

    override suspend fun deleteEvent(eventId: String) {
        getOwnedEvent(eventId)
        events.document(eventId).delete().await()
    }
}
