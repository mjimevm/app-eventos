package plat.proyecto.guatevivo.core.firebase.funciones.event.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class EventFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : EventFirebase {

    private val events
        get() = firestore.collection(FirebaseCollections.EVENTS)

    private val users
        get() = firestore.collection(FirebaseCollections.USERS)

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

    override suspend fun getPublicEvents(): List<EventDto> {
        return events
            .whereEqualTo("status", EventStatus.PUBLISHED.name)
            .whereEqualTo("visibility", EventVisibility.PUBLIC.name)
            .get()
            .await()
            .toObjects(EventDto::class.java)
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

    override suspend fun getEventById(eventId: String): EventDto? {
        return events.document(eventId)
            .get()
            .await()
            .toObject(EventDto::class.java)
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