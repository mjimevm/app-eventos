package plat.proyecto.guatevivo.testing

import kotlinx.coroutines.CompletableDeferred
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.PublicEventsResult

fun publicEvent(id: String, date: String = "20/11/2026", time: String = "18:30") = EventDto(
    id = id, nombreEvento = "Evento $id", fecha = date, hora = time,
    status = "PUBLISHED", visibility = "PUBLIC"
)

class FakeEventRepository : EventRepository {
    var events = emptyList<EventDto>()
    var isFromCache = false
    var failure: Exception? = null
    var gate: CompletableDeferred<Unit>? = null
    var publicCalls = 0
    var requestedId: String? = null
    var mutationFailure: Exception? = null
    var uploadFailure: Exception? = null
    var mutationGate: CompletableDeferred<Unit>? = null
    val createdDrafts = mutableListOf<EventDto>()
    val updatedDrafts = mutableListOf<EventDto>()
    val updatedEvents = mutableListOf<EventDto>()
    val deletedIds = mutableListOf<String>()
    val imageUploads = mutableListOf<Pair<String, String>>()
    var uploadedImageUrl = "https://storage.example.test/new-image.jpg"

    override suspend fun getPublicEvents(): List<EventDto> {
        publicCalls++
        gate?.await()
        failure?.let { throw it }
        return events.toList()
    }
    override suspend fun getPublicEventsResult() = PublicEventsResult(getPublicEvents(), isFromCache)
    override suspend fun getEventById(eventId: String): EventDto? {
        requestedId = eventId
        failure?.let { throw it }
        return events.find { it.id == eventId }
    }
    override suspend fun getEventForSharing(eventId: String): EventDto? = getEventById(eventId)?.takeIf {
        it.status == "PUBLISHED" && it.visibility == "PUBLIC"
    }
    private suspend fun beforeMutation() {
        mutationGate?.await()
        mutationFailure?.let { throw it }
    }
    override suspend fun uploadEventImage(eventId: String, imageUri: String): String {
        imageUploads.add(eventId to imageUri)
        uploadFailure?.let { throw it }
        return uploadedImageUrl
    }
    override suspend fun createDraft(event: EventDto, imageUri: String?): String {
        beforeMutation()
        createdDrafts.add(event)
        return "new-draft"
    }
    override suspend fun updateDraft(event: EventDto) {
        beforeMutation()
        updatedDrafts.add(event)
        events = events.map { if (it.id == event.id) event else it }
    }
    override suspend fun publishEvent(eventId: String): Unit = error("Unexpected call")
    override suspend fun getMyDrafts(): List<EventDto> = error("Unexpected call")
    override suspend fun getMyCreatedEvents(): List<EventDto> = error("Unexpected call")
    override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = error("Unexpected call")
    override suspend fun updateEvent(event: EventDto) {
        beforeMutation()
        updatedEvents.add(event)
        events = events.map { if (it.id == event.id) event else it }
    }
    override suspend fun deleteEvent(eventId: String) {
        beforeMutation()
        deletedIds.add(eventId)
        events = events.filterNot { it.id == eventId }
    }
}
