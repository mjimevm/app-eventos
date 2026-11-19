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
import plat.proyecto.guatevivo.core.repository.EventRepository

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
