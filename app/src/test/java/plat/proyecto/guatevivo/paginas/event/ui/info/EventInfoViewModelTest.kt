package plat.proyecto.guatevivo.paginas.event.ui.info

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.AttendanceRepository

@OptIn(ExperimentalCoroutinesApi::class)
class EventInfoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    private class FakeRepository : EventRepository {
        var requestedId: String? = null
        var failure: Exception? = null
        var event: EventDto? = EventDto(
            id = "firebase-document-id", nombreEvento = "Evento real",
            fecha = "19/11/2026", hora = "18:30", ubicacion = "Antigua",
            descripcion = "Descripcion real", imagenUrl = "https://example.com/event.jpg"
        )
        override suspend fun getEventById(eventId: String): EventDto? {
            requestedId = eventId
            failure?.let { throw it }
            return event
        }
        override suspend fun createDraft(event: EventDto, imageUri: String?): String = error("Unexpected call")
        override suspend fun updateDraft(event: EventDto): Unit = error("Unexpected call")
        override suspend fun publishEvent(eventId: String): Unit = error("Unexpected call")
        override suspend fun getPublicEvents(): List<EventDto> = error("Unexpected call")
        override suspend fun getMyDrafts(): List<EventDto> = error("Unexpected call")
        override suspend fun getMyCreatedEvents(): List<EventDto> = error("Unexpected call")
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = error("Unexpected call")
        override suspend fun updateEvent(event: EventDto): Unit = error("Unexpected call")
        override suspend fun deleteEvent(eventId: String): Unit = error("Unexpected call")
    }

    private class FakeAttendanceFirebase : AttendanceFirebase {
        var attending = false
        var total = 3
        var friends = 1
        var confirmCalls = 0
        var cancelCalls = 0
        var failure: Exception? = null
        var busyGate: kotlinx.coroutines.CompletableDeferred<Unit>? = null

        override suspend fun confirmAttendance(eventId: String) {
            busyGate?.await()
            failure?.let { throw it }
            confirmCalls++
            attending = true
            total++
            friends++
        }
        override suspend fun cancelAttendance(eventId: String) {
            busyGate?.await()
            failure?.let { throw it }
            cancelCalls++
            attending = false
            total = (total - 1).coerceAtLeast(0)
            friends = (friends - 1).coerceAtLeast(0)
        }
        override suspend fun isAttending(eventId: String): Boolean = attending
        override suspend fun getTotalAttendees(eventId: String): Int = total
        override suspend fun getAttendingFriendsCount(eventId: String): Int = friends
        override suspend fun getAttendingEventIds(): List<String> =
            if (attending) listOf("firebase-document-id") else emptyList()
        override suspend fun getAttendanceStats(eventId: String): AttendanceStatsDto {
            failure?.let { throw it }
            return AttendanceStatsDto(total, friends, attending)
        }
    }

    private fun viewModel(repository: EventRepository) = EventInfoViewModel(
        EventInfoDestination(eventId = "firebase-document-id"), repository
    )

    @Test fun realEvent_loadsByOriginalIdAndDisplaysContent() = runTest {
        val repository = FakeRepository()
        val viewModel = viewModel(repository)
        assertTrue(viewModel.state.value.isLoading)
        dispatcher.scheduler.advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals("firebase-document-id", repository.requestedId)
        assertEquals("Evento real", state.titulo)
        assertEquals("Antigua", state.ubicacion)
        assertEquals("Descripcion real", state.descripcionLarga)
        assertEquals("https://example.com/event.jpg", state.imageUrl)
        assertEquals("19", state.dia)
        assertEquals("noviembre 2026", state.mes)
        assertTrue(state.miniDescripcion.contains("18:30"))
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test fun missingEvent_displaysErrorWithoutSampleContent() = runTest {
        val repository = FakeRepository().apply { event = null }
        val viewModel = viewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertEquals("", viewModel.state.value.titulo)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test fun repositoryFailure_displaysError() = runTest {
        val repository = FakeRepository().apply { failure = IllegalStateException("Sin conexion") }
        val viewModel = viewModel(repository)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals("Sin conexion", viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test fun publishedEventWithConfirmationShowsAndLoadsAttendance() = runTest {
        val eventRepository = FakeRepository().apply {
            event = event!!.copy(status = EventStatus.PUBLISHED.name, incluirConfirmacion = true)
        }
        val attendance = FakeAttendanceFirebase()
        val viewModel = EventInfoViewModel(
            EventInfoDestination(eventId = "firebase-document-id"),
            eventRepository,
            AttendanceRepository(attendance),
            { "uid-1" }
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.canAttend)
        assertEquals(3, viewModel.state.value.totalAttendees)
        assertEquals(1, viewModel.state.value.attendingFriendsCount)
    }

    @Test fun publishedEventWithoutConfirmationAndDraftHideAttendance() = runTest {
        val attendance = FakeAttendanceFirebase()
        val noConfirmation = FakeRepository().apply {
            event = event!!.copy(status = EventStatus.PUBLISHED.name, incluirConfirmacion = false)
        }
        val vm = EventInfoViewModel(EventInfoDestination(eventId = "firebase-document-id"), noConfirmation, AttendanceRepository(attendance), { "uid-1" })
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.canAttend)

        val draft = FakeRepository().apply {
            event = event!!.copy(status = "DRAFT", incluirConfirmacion = true)
        }
        val draftVm = EventInfoViewModel(EventInfoDestination(eventId = "firebase-document-id"), draft, AttendanceRepository(attendance), { "uid-1" })
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(draftVm.state.value.canAttend)
    }

    @Test fun confirmAndCancelUpdateStateAndCounters() = runTest {
        val eventRepository = FakeRepository().apply {
            event = event!!.copy(status = EventStatus.PUBLISHED.name, incluirConfirmacion = true)
        }
        val attendance = FakeAttendanceFirebase()
        val vm = EventInfoViewModel(EventInfoDestination(eventId = "firebase-document-id"), eventRepository, AttendanceRepository(attendance), { "uid-1" })
        dispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(EventInfoEvent.ToggleAttend)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isAttending)
        assertEquals(4, vm.state.value.totalAttendees)
        assertEquals(1, attendance.confirmCalls)

        vm.onEvent(EventInfoEvent.ToggleAttend)
        dispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isAttending)
        assertEquals(3, vm.state.value.totalAttendees)
        assertEquals(1, attendance.cancelCalls)
    }

    @Test fun attendanceErrorIsVisibleAndDuplicateOperationIsBlocked() = runTest {
        val eventRepository = FakeRepository().apply {
            event = event!!.copy(status = EventStatus.PUBLISHED.name, incluirConfirmacion = true)
        }
        val attendance = FakeAttendanceFirebase().apply {
            busyGate = kotlinx.coroutines.CompletableDeferred()
        }
        val vm = EventInfoViewModel(EventInfoDestination(eventId = "firebase-document-id"), eventRepository, AttendanceRepository(attendance), { "uid-1" })
        dispatcher.scheduler.advanceUntilIdle()
        vm.onEvent(EventInfoEvent.ToggleAttend)
        vm.onEvent(EventInfoEvent.ToggleAttend)
        assertTrue(vm.state.value.isAttendanceLoading)
        attendance.busyGate!!.complete(Unit)
        dispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, attendance.confirmCalls)

        attendance.failure = IllegalStateException("Firebase denegó la operación")
        vm.onEvent(EventInfoEvent.ToggleAttend)
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.error!!.contains("Firebase denegó la operación"))
    }

    @Test fun sampleEvents_preserveExistingNavigation() = runTest {
        val repository = FakeRepository()
        val viewModel = EventInfoViewModel(
            EventInfoDestination(mockEventId = 1), repository
        )
        dispatcher.scheduler.advanceUntilIdle()
        assertNull(repository.requestedId)
        assertTrue(viewModel.state.value.titulo.isNotBlank())
        assertFalse(viewModel.state.value.isLoading)
    }
}
