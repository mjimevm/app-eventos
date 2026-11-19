package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto

interface EventRepository {
    suspend fun createDraft(event: EventDto, imageUri: String? = null): String
    suspend fun updateDraft(event: EventDto)
    suspend fun publishEvent(eventId: String)
    suspend fun getPublicEvents(): List<EventDto>
    suspend fun getPublicEventsResult(): PublicEventsResult = PublicEventsResult(getPublicEvents())
    suspend fun getMyDrafts(): List<EventDto>
    suspend fun getMyCreatedEvents(): List<EventDto>
    suspend fun getEventById(eventId: String): EventDto?
    /** Sharing requires a fresh visibility check; implementations must not use an offline fallback. */
    suspend fun getEventForSharing(eventId: String): EventDto? =
        throw UnsupportedOperationException("No se puede verificar si el evento se puede compartir")
    suspend fun uploadEventImage(eventId: String, imageUri: String): String =
        throw UnsupportedOperationException("No se puede actualizar la imagen del evento")
    suspend fun getEventsByOrganizer(organizerId: String): List<EventDto>
    suspend fun updateEvent(event: EventDto)
    suspend fun deleteEvent(eventId: String)
}
