package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto

interface EventRepository {
    suspend fun createDraft(event: EventDto): String
    suspend fun updateDraft(event: EventDto)
    suspend fun publishEvent(eventId: String)
    suspend fun getPublicEvents(): List<EventDto>
    suspend fun getMyDrafts(): List<EventDto>
    suspend fun getMyCreatedEvents(): List<EventDto>
    suspend fun getEventById(eventId: String): EventDto?
    suspend fun getEventsByOrganizer(organizerId: String): List<EventDto>
    suspend fun updateEvent(event: EventDto)
    suspend fun deleteEvent(eventId: String)
}
