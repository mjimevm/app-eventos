package plat.proyecto.guatevivo.core.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import plat.proyecto.guatevivo.core.firebase.funciones.event.firebase.EventFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import plat.proyecto.guatevivo.core.firebase.funciones.storage.StorageFirebase
import plat.proyecto.guatevivo.core.room.dao.EventDao
import plat.proyecto.guatevivo.core.room.mapper.toDto
import plat.proyecto.guatevivo.core.room.mapper.toDtos
import plat.proyecto.guatevivo.core.room.mapper.toEntities
import plat.proyecto.guatevivo.core.room.mapper.toEntity

class EventRepositoryImpl(
    private val eventFirebase: EventFirebase,
    private val eventDao: EventDao,
    private val storageFirebase: StorageFirebase? = null,
    private val currentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid }
) : EventRepository {
    private val publicEventsMutex = Mutex()

    private fun getCurrentUid(): String? {
        return currentUserId()
    }

    override suspend fun createDraft(event: EventDto, imageUri: String?): String {
        val eventId = eventFirebase.createDraft(event)
        val uid = getCurrentUid() ?: event.organizadorId
        var finalImageUrl = event.imagenUrl

        if (!imageUri.isNullOrBlank() && (imageUri.startsWith("content://") || imageUri.startsWith("file://"))) {
            if (storageFirebase != null) {
                try {
                    finalImageUrl = storageFirebase.uploadEventImage(eventId, Uri.parse(imageUri))
                } catch (e: Exception) {
                    android.util.Log.e("GUATEVIVO_EVENT", "Error al subir imagen a Firebase Storage", e)
                }
            }
        }

        val savedDraft = event.copy(
            id = eventId,
            organizadorId = uid,
            imagenUrl = finalImageUrl,
            status = EventStatus.DRAFT.name,
            visibility = EventVisibility.PRIVATE.name,
            createdAt = if (event.createdAt == 0L) System.currentTimeMillis() else event.createdAt
        )

        if (finalImageUrl != event.imagenUrl) {
            try {
                eventFirebase.updateDraft(savedDraft)
            } catch (e: Exception) {
                android.util.Log.e("GUATEVIVO_EVENT", "Error al actualizar borrador con URL remota", e)
            }
        }

        eventDao.insertEvent(savedDraft.toEntity())
        return eventId
    }

    override suspend fun updateDraft(event: EventDto) {
        publicEventsMutex.withLock {
            eventFirebase.updateDraft(event)
            eventDao.insertEvent(event.toEntity())
        }
    }

    override suspend fun getEventForSharing(eventId: String): EventDto? =
        eventFirebase.getEventById(eventId)?.takeIf {
            it.status == EventStatus.PUBLISHED.name && it.visibility == EventVisibility.PUBLIC.name
        }

    override suspend fun uploadEventImage(eventId: String, imageUri: String): String {
        require(imageUri.startsWith("content://") || imageUri.startsWith("file://")) { "Imagen no válida" }
        val event = eventFirebase.getEventById(eventId) ?: error("El evento no existe")
        val uid = getCurrentUid() ?: error("Debes iniciar sesión")
        require(event.organizadorId == uid) { "No puedes modificar eventos de otros usuarios" }
        val storage = checkNotNull(storageFirebase) { "No se puede actualizar la imagen del evento" }
        return storage.uploadEventImageVersion(eventId, Uri.parse(imageUri))
    }

    override suspend fun publishEvent(eventId: String) {
        eventFirebase.publishEvent(eventId)
        val updated = eventFirebase.getEventById(eventId)
        if (updated != null) {
            eventDao.insertEvent(updated.toEntity())
        }
    }

    override suspend fun getPublicEvents(): List<EventDto> {
        return getPublicEventsResult().events
    }

    override suspend fun getPublicEventsResult(): PublicEventsResult = publicEventsMutex.withLock {
        val remote = try {
            eventFirebase.getPublicEvents()
        } catch (e: Exception) {
            if (!e.isEventConnectionFailure()) throw e
            return@withLock PublicEventsResult(publicOnly(eventDao.getPublicEvents().toDtos()), isFromCache = true)
        }
        val events = publicOnly(remote)
        // A successful server response is authoritative, including an empty catalogue.
        // Replace only public rows; private drafts remain untouched.
        eventDao.replacePublicEvents(events.toEntities())
        PublicEventsResult(events)
    }

    private fun publicOnly(events: List<EventDto>): List<EventDto> = events.filter {
        it.id.isNotBlank() && it.status == EventStatus.PUBLISHED.name && it.visibility == EventVisibility.PUBLIC.name
    }.distinctBy { it.id }

    override suspend fun getMyDrafts(): List<EventDto> {
        val uid = getCurrentUid()
        return try {
            val remote = eventFirebase.getMyDrafts()
            if (remote.isNotEmpty()) {
                eventDao.insertEvents(remote.toEntities())
            }
            val local = if (uid != null) eventDao.getDrafts(uid).toDtos() else emptyList()
            (remote + local).distinctBy { it.id }.filter {
                it.status == EventStatus.DRAFT.name && (uid == null || it.organizadorId == uid)
            }
        } catch (e: Exception) {
            if (uid != null) {
                eventDao.getDrafts(uid).toDtos()
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getMyCreatedEvents(): List<EventDto> {
        val uid = getCurrentUid()
        return try {
            val remote = eventFirebase.getMyCreatedEvents()
            if (remote.isNotEmpty()) {
                eventDao.insertEvents(remote.toEntities())
            }
            val local = if (uid != null) eventDao.getEventsByOrganizer(uid).toDtos() else emptyList()
            (remote + local).distinctBy { it.id }.filter {
                uid == null || it.organizadorId == uid
            }
        } catch (e: Exception) {
            if (uid != null) {
                eventDao.getEventsByOrganizer(uid).toDtos()
            } else {
                emptyList()
            }
        }
    }

    override suspend fun getEventById(eventId: String): EventDto? {
        val remote = try {
            eventFirebase.getEventById(eventId)
        } catch (e: Exception) {
            if (!e.isEventConnectionFailure()) throw e
            return eventDao.getEventById(eventId)?.toDto()
        }
        if (remote != null) eventDao.insertEvent(remote.toEntity())
        else eventDao.deleteEvent(eventId)
        return remote
    }

    override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> {
        return try {
            val remote = eventFirebase.getEventsByOrganizer(organizerId)
            if (remote.isNotEmpty()) {
                eventDao.insertEvents(remote.toEntities())
            }
            val local = eventDao.getEventsByOrganizer(organizerId).toDtos()
            (remote + local).distinctBy { it.id }.filter { it.organizadorId == organizerId }
        } catch (e: Exception) {
            eventDao.getEventsByOrganizer(organizerId).toDtos()
        }
    }

    override suspend fun updateEvent(event: EventDto) {
        publicEventsMutex.withLock {
            eventFirebase.updateEvent(event)
            eventDao.insertEvent(event.toEntity())
        }
    }

    override suspend fun deleteEvent(eventId: String) {
        publicEventsMutex.withLock {
            eventFirebase.deleteEvent(eventId)
            eventDao.deleteEvent(eventId)
        }
    }
}
