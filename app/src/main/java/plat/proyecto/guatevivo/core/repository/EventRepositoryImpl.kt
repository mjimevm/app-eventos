package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.event.firebase.EventFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.room.dao.EventDao
import plat.proyecto.guatevivo.core.room.mapper.toDto
import plat.proyecto.guatevivo.core.room.mapper.toDtos
import plat.proyecto.guatevivo.core.room.mapper.toEntities
import plat.proyecto.guatevivo.core.room.mapper.toEntity

class EventRepositoryImpl(
    private val eventFirebase: EventFirebase,
    private val eventDao: EventDao,
) : EventRepository {

    override suspend fun createDraft(event: EventDto): String {
        val eventId = eventFirebase.createDraft(event)
        val created = eventFirebase.getEventById(eventId)
        if (created != null) {
            eventDao.insertEvent(created.toEntity())
        }
        return eventId
    }

    override suspend fun updateDraft(event: EventDto) {
        eventFirebase.updateDraft(event)
        eventDao.insertEvent(event.toEntity())
    }

    override suspend fun publishEvent(eventId: String) {
        eventFirebase.publishEvent(eventId)
        val updated = eventFirebase.getEventById(eventId)
        if (updated != null) {
            eventDao.insertEvent(updated.toEntity())
        }
    }

    override suspend fun getPublicEvents(): List<EventDto> {
        return try {
            val remote = eventFirebase.getPublicEvents()
            eventDao.insertEvents(remote.toEntities())
            remote
        } catch (e: Exception) {
            eventDao.getPublicEvents().toDtos()
        }
    }

    override suspend fun getMyDrafts(): List<EventDto> {
        return try {
            val remote = eventFirebase.getMyDrafts()
            eventDao.insertEvents(remote.toEntities())
            remote
        } catch (e: Exception) {
            val local = eventDao.getAllEvents().toDtos()
            local.filter { it.status == "DRAFT" }
        }
    }

    override suspend fun getMyCreatedEvents(): List<EventDto> {
        return try {
            val remote = eventFirebase.getMyCreatedEvents()
            eventDao.insertEvents(remote.toEntities())
            remote
        } catch (e: Exception) {
            eventDao.getAllEvents().toDtos()
        }
    }

    override suspend fun getEventById(eventId: String): EventDto? {
        return try {
            val remote = eventFirebase.getEventById(eventId)
            if (remote != null) {
                eventDao.insertEvent(remote.toEntity())
            }
            remote
        } catch (e: Exception) {
            eventDao.getEventById(eventId)?.toDto()
        }
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> {
        return try {
            val remote = eventFirebase.getEventsByOrganizer(organizerId)
            eventDao.insertEvents(remote.toEntities())
            remote
        } catch (e: Exception) {
            eventDao.getEventsByOrganizer(organizerId).toDtos()
        }
    }

    override suspend fun updateEvent(event: EventDto) {
        eventFirebase.updateEvent(event)
        eventDao.insertEvent(event.toEntity())
    }

    override suspend fun deleteEvent(eventId: String) {
        eventFirebase.deleteEvent(eventId)
        eventDao.deleteEvent(eventId)
    }
}
