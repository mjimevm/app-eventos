package plat.proyecto.guatevivo.core.repository

import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.event.firebase.EventFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.room.dao.EventDao
import plat.proyecto.guatevivo.core.room.entity.EventEntity
import plat.proyecto.guatevivo.core.room.mapper.toEntity
import plat.proyecto.guatevivo.testing.publicEvent

@OptIn(ExperimentalCoroutinesApi::class)
class EventRepositoryImplTest {
    private class FakeFirebase : EventFirebase {
        var events = emptyList<EventDto>()
        var failure: Exception? = null
        var publicGate: CompletableDeferred<Unit>? = null
        override suspend fun getPublicEvents(): List<EventDto> {
            val snapshot = events
            publicGate?.await()
            failure?.let { throw it }
            return snapshot
        }
        override suspend fun getEventById(eventId: String): EventDto? {
            failure?.let { throw it }
            return events.find { it.id == eventId }
        }
        override suspend fun createDraft(event: EventDto): String = error("Unexpected call")
        override suspend fun updateDraft(event: EventDto) = updateEvent(event)
        override suspend fun publishEvent(eventId: String): Unit = error("Unexpected call")
        override suspend fun getMyDrafts(): List<EventDto> = error("Unexpected call")
        override suspend fun getMyCreatedEvents(): List<EventDto> = error("Unexpected call")
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = error("Unexpected call")
        override suspend fun updateEvent(event: EventDto) {
            failure?.let { throw it }
            events = events.map { if (it.id == event.id) event else it }
        }
        override suspend fun deleteEvent(eventId: String) {
            failure?.let { throw it }
            events = events.filterNot { it.id == eventId }
        }
    }

    private class FakeDao : EventDao {
        val rows = linkedMapOf<String, EventEntity>()
        var cacheReads = 0
        var writeFailure: Exception? = null
        override fun getAllEventsFlow(): Flow<List<EventEntity>> = flowOf(rows.values.toList())
        override suspend fun getAllEvents() = rows.values.toList()
        override suspend fun getEventById(eventId: String): EventEntity? {
            cacheReads++
            return rows[eventId]
        }
        override suspend fun getPublicEvents(): List<EventEntity> {
            cacheReads++
            return rows.values.filter { it.status == "PUBLISHED" && it.visibility == "PUBLIC" }
        }
        override suspend fun deletePublicEvents() {
            writeFailure?.let { throw it }
            rows.entries.removeAll { it.value.status == "PUBLISHED" && it.value.visibility == "PUBLIC" }
        }
        override suspend fun getDrafts(organizerId: String) = rows.values.filter { it.organizadorId == organizerId && it.status == "DRAFT" }
        override suspend fun getEventsByOrganizer(organizerId: String) = rows.values.filter { it.organizadorId == organizerId }
        override suspend fun insertEvent(event: EventEntity) { rows[event.id] = event }
        override suspend fun insertEvents(events: List<EventEntity>) { events.forEach { insertEvent(it) } }
        override suspend fun deleteEvent(eventId: String) { rows.remove(eventId) }
        override suspend fun clearAll() { rows.clear() }
    }

    @Test fun serverCatalogueReplacesStalePublicRowsAndPreservesPrivateDrafts() = runTest {
        val firebase = FakeFirebase().apply {
            events = listOf(
                publicEvent("current"), publicEvent("current"), publicEvent("draft").copy(status = "DRAFT"),
                publicEvent("private").copy(visibility = "PRIVATE"), publicEvent("cancelled").copy(status = "CANCELLED")
            )
        }
        val dao = FakeDao().apply {
            insertEvent(publicEvent("deleted").toEntity())
            insertEvent(publicEvent("my-draft").copy(status = "DRAFT", visibility = "PRIVATE").toEntity())
        }
        val result = EventRepositoryImpl(firebase, dao).getPublicEventsResult()
        assertFalse(result.isFromCache)
        assertEquals(listOf("current"), result.events.map { it.id })
        assertEquals(setOf("my-draft", "current"), dao.rows.keys)
    }

    @Test fun emptyServerCataloguePurgesPublicCacheAndNeverAddsSamples() = runTest {
        val dao = FakeDao().apply { insertEvent(publicEvent("deleted").toEntity()) }
        val result = EventRepositoryImpl(FakeFirebase(), dao).getPublicEventsResult()
        assertFalse(result.isFromCache)
        assertTrue(result.events.isEmpty())
        assertTrue(dao.rows.isEmpty())
    }

    @Test fun connectionFailureUsesOnlyPublishedPublicRoomRowsAndReportsSource() = runTest {
        val firebase = FakeFirebase().apply { failure = IOException("offline") }
        val dao = FakeDao().apply {
            insertEvent(publicEvent("cached").toEntity())
            insertEvent(publicEvent("private").copy(visibility = "PRIVATE").toEntity())
            insertEvent(publicEvent("draft").copy(status = "DRAFT").toEntity())
        }
        val result = EventRepositoryImpl(firebase, dao).getPublicEventsResult()
        assertTrue(result.isFromCache)
        assertEquals(listOf("cached"), result.events.map { it.id })
    }

    @Test fun remoteConnectionFailureUsesRoom() = runTest {
        val firebase = FakeFirebase().apply {
            failure = EventReadException(EventReadFailure.CONNECTION, IllegalStateException("offline"))
        }
        val dao = FakeDao().apply { insertEvent(publicEvent("cached").toEntity()) }
        assertTrue(EventRepositoryImpl(firebase, dao).getPublicEventsResult().isFromCache)
    }

    @Test fun permissionAndAuthenticationErrorsNeverFallBackToRoom() = runTest {
        for (reason in listOf(EventReadFailure.PERMISSION_DENIED, EventReadFailure.UNAUTHENTICATED)) {
            val denied = EventReadException(reason, IllegalStateException("denied"))
            val firebase = FakeFirebase().apply { failure = denied }
            val dao = FakeDao().apply { insertEvent(publicEvent("cached").toEntity()) }
            try {
                EventRepositoryImpl(firebase, dao).getPublicEventsResult()
                fail("Expected permission/authentication failure")
            } catch (error: EventReadException) { assertSame(denied, error) }
            assertEquals(0, dao.cacheReads)
        }
    }

    @Test fun localDatabaseFailureIsNotReportedAsOffline() = runTest {
        val failure = IOException("database write failure")
        val dao = FakeDao().apply { writeFailure = failure }
        try {
            EventRepositoryImpl(FakeFirebase(), dao).getPublicEventsResult()
            fail("Expected database failure")
        } catch (error: IOException) { assertSame(failure, error) }
        assertEquals(0, dao.cacheReads)
    }

    @Test fun deletedOrUnpublishedEventDoesNotReappearAfterSuccessfulSyncThenOfflineLoad() = runTest {
        val firebase = FakeFirebase().apply { events = listOf(publicEvent("removed")) }
        val dao = FakeDao()
        val repository = EventRepositoryImpl(firebase, dao)
        assertEquals(1, repository.getPublicEvents().size)
        firebase.events = emptyList()
        assertTrue(repository.getPublicEvents().isEmpty())
        firebase.failure = IOException("offline")
        val offline = repository.getPublicEventsResult()
        assertTrue(offline.isFromCache)
        assertTrue(offline.events.isEmpty())
    }

    @Test fun deletedDetailIsEvictedAndCannotBeResurrectedOffline() = runTest {
        val firebase = FakeFirebase()
        val dao = FakeDao().apply { insertEvent(publicEvent("deleted").toEntity()) }
        val repository = EventRepositoryImpl(firebase, dao)
        assertNull(repository.getEventById("deleted"))
        firebase.failure = IOException("offline")
        assertNull(repository.getEventById("deleted"))
    }

    @Test fun detailOfflineUsesCacheButPermissionFailureIsPropagated() = runTest {
        val firebase = FakeFirebase().apply { failure = IOException("offline") }
        val dao = FakeDao().apply { insertEvent(publicEvent("cached").toEntity()) }
        val repository = EventRepositoryImpl(firebase, dao)
        assertEquals("cached", repository.getEventById("cached")?.id)
        firebase.failure = EventReadException(EventReadFailure.PERMISSION_DENIED, IllegalStateException("denied"))
        try {
            repository.getEventById("cached")
            fail("Expected permission failure")
        } catch (_: EventReadException) { }
        assertEquals(1, dao.cacheReads)
    }

    @Test fun cancellationIsPropagatedWithoutReadingCache() = runTest {
        val firebase = FakeFirebase().apply { failure = CancellationException("cancelled") }
        val dao = FakeDao()
        try {
            EventRepositoryImpl(firebase, dao).getPublicEventsResult()
            fail("Expected cancellation")
        } catch (_: CancellationException) { }
        assertEquals(0, dao.cacheReads)
    }

    @Test fun deletionUpdatesRemoteAndRoomAndFailedDeletionKeepsLocalEvent() = runTest {
        val event = publicEvent("owned")
        val firebase = FakeFirebase().apply { events = listOf(event) }
        val dao = FakeDao().apply { insertEvent(event.toEntity()) }
        val repository = EventRepositoryImpl(firebase, dao)
        firebase.failure = IllegalStateException("No autorizado")
        try {
            repository.deleteEvent(event.id)
            fail("Expected rejected deletion")
        } catch (_: IllegalStateException) { }
        assertTrue(dao.rows.containsKey(event.id))
        firebase.failure = null
        repository.deleteEvent(event.id)
        assertTrue(firebase.events.isEmpty())
        assertTrue(dao.rows.isEmpty())
    }

    @Test fun successfulUpdatesReachRoomAndFailedUpdatesLeaveItUntouched() = runTest {
        for (status in listOf("DRAFT", "PUBLISHED")) {
            val event = publicEvent("owned").copy(status = status, organizadorId = "owner", createdAt = 42L)
            val firebase = FakeFirebase().apply { events = listOf(event) }
            val dao = FakeDao().apply { insertEvent(event.toEntity()) }
            val repository = EventRepositoryImpl(firebase, dao)
            val edited = event.copy(nombreEvento = "Editado")
            firebase.failure = IllegalStateException("No autorizado")
            try {
                if (status == "DRAFT") repository.updateDraft(edited) else repository.updateEvent(edited)
                fail("Expected rejected update")
            } catch (_: IllegalStateException) { }
            assertEquals(event.toEntity(), dao.rows[event.id])
            firebase.failure = null
            if (status == "DRAFT") repository.updateDraft(edited) else repository.updateEvent(edited)
            assertEquals(edited.toEntity(), dao.rows[event.id])
        }
    }

    @Test fun sharingRequiresServerAndNeverUsesOfflinePublicCache() = runTest {
        val event = publicEvent("cached")
        val firebase = FakeFirebase().apply { failure = IOException("offline") }
        val dao = FakeDao().apply { insertEvent(event.toEntity()) }
        val repository = EventRepositoryImpl(firebase, dao)
        try {
            repository.getEventForSharing(event.id)
            fail("Sharing requires a fresh visibility check")
        } catch (_: IOException) { }
        assertEquals(0, dao.cacheReads)
        firebase.failure = null
        for (remote in listOf(event.copy(status = "DRAFT"), event.copy(visibility = "PRIVATE"), event.copy(status = "CANCELLED"))) {
            firebase.events = listOf(remote)
            assertNull(repository.getEventForSharing(event.id))
        }
        firebase.events = listOf(event)
        assertEquals(event, repository.getEventForSharing(event.id))
    }

    @Test fun imageUploadChecksOwnershipBeforeAccessingStorage() = runTest {
        val firebase = FakeFirebase().apply { events = listOf(publicEvent("owned").copy(organizadorId = "owner")) }
        val repository = EventRepositoryImpl(firebase, FakeDao(), currentUserId = { "other" })
        try {
            repository.uploadEventImage("owned", "content://photos/selected")
            fail("Expected ownership failure")
        } catch (error: IllegalArgumentException) {
            assertEquals("No puedes modificar eventos de otros usuarios", error.message)
        }
    }

    @Test fun inFlightCatalogueCannotResurrectEventAfterDeletion() = runTest {
        val event = publicEvent("owned")
        val firebase = FakeFirebase().apply { events = listOf(event); publicGate = CompletableDeferred() }
        val dao = FakeDao().apply { insertEvent(event.toEntity()) }
        val repository = EventRepositoryImpl(firebase, dao)
        launch { repository.getPublicEvents() }
        runCurrent()
        launch { repository.deleteEvent(event.id) }
        runCurrent()
        firebase.publicGate!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(dao.rows.containsKey(event.id))
        assertTrue(firebase.events.isEmpty())
    }
}
