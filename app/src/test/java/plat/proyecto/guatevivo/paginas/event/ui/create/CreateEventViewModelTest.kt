package plat.proyecto.guatevivo.paginas.event.ui.create

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.repository.EventRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CreateEventViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeEventRepository : EventRepository {
        var createdDraft: EventDto? = null
        var passedImageUri: String? = null

        override suspend fun createDraft(event: EventDto, imageUri: String?): String {
            createdDraft = event
            passedImageUri = imageUri
            return "event_id_123"
        }

        override suspend fun updateDraft(event: EventDto) {}
        override suspend fun publishEvent(eventId: String) {}
        override suspend fun getPublicEvents(): List<EventDto> = emptyList()
        override suspend fun getMyDrafts(): List<EventDto> = emptyList()
        override suspend fun getMyCreatedEvents(): List<EventDto> = emptyList()
        override suspend fun getEventById(eventId: String): EventDto? = null
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = emptyList()
        override suspend fun updateEvent(event: EventDto) {}
        override suspend fun deleteEvent(eventId: String) {}
    }

    @Test
    fun isValidDate_validDates_returnsTrue() {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        assertTrue(viewModel.isValidDate("21/02/2027"))
        assertTrue(viewModel.isValidDate("05/12/2026"))
        assertTrue(viewModel.isValidDate("29/02/2028")) // Bisiesto
        assertTrue(viewModel.isValidDate("01/01/2028"))
    }

    @Test
    fun isValidDate_invalidDates_returnsFalse() {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        assertFalse(viewModel.isValidDate("31/02/2027")) // Febrero 31
        assertFalse(viewModel.isValidDate("29/02/2027")) // 2027 no es bisiesto
        assertFalse(viewModel.isValidDate("2027/02/21")) // Formato equivocado
        assertFalse(viewModel.isValidDate("invalid"))
        assertFalse(viewModel.isValidDate(""))
    }

    @Test
    fun isValidTime_validTimes_returnsTrue() {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        assertTrue(viewModel.isValidTime("11:22"))
        assertTrue(viewModel.isValidTime("08:05"))
        assertTrue(viewModel.isValidTime("14:30"))
        assertTrue(viewModel.isValidTime("23:59"))
        assertTrue(viewModel.isValidTime("00:00"))
    }

    @Test
    fun isValidTime_invalidTimes_returnsFalse() {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        assertFalse(viewModel.isValidTime("25:80"))
        assertFalse(viewModel.isValidTime("14:60"))
        assertFalse(viewModel.isValidTime("8:05")) // Debe tener 2 dígitos
        assertFalse(viewModel.isValidTime("invalid"))
        assertFalse(viewModel.isValidTime(""))
    }

    @Test
    fun clearSuccess_resetsSuccessAndClearsFormFields() = runTest {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        // Set form fields
        viewModel.onEvent(CreateEventEvent.UpdateNombreEvento("Cena Navideña"))
        viewModel.onEvent(CreateEventEvent.UpdateUbicacion("Guatemala"))
        viewModel.onEvent(CreateEventEvent.UpdateFecha("24/12/2026"))
        viewModel.onEvent(CreateEventEvent.UpdateHora("20:00"))
        viewModel.onEvent(CreateEventEvent.UpdateDescripcion("Cena de fin de año"))
        viewModel.onEvent(CreateEventEvent.UpdateImageUri("content://media/external/images/media/123"))

        assertEquals("Cena Navideña", viewModel.state.value.nombreEvento)

        // Clear success
        viewModel.onEvent(CreateEventEvent.ClearSuccess)

        val stateAfterClear = viewModel.state.value
        assertFalse(stateAfterClear.isSuccess)
        assertEquals("", stateAfterClear.nombreEvento)
        assertEquals("", stateAfterClear.ubicacion)
        assertEquals("", stateAfterClear.fecha)
        assertEquals("", stateAfterClear.hora)
        assertEquals("", stateAfterClear.descripcion)
        assertEquals(null, stateAfterClear.imageUri)
    }

    @Test
    fun updateImageUri_updatesStateAndCanBeCleared() {
        val repository = FakeEventRepository()
        val viewModel = CreateEventViewModel(repository)

        viewModel.onEvent(CreateEventEvent.UpdateImageUri("content://media/external/images/media/1"))
        assertEquals("content://media/external/images/media/1", viewModel.state.value.imageUri)

        viewModel.onEvent(CreateEventEvent.UpdateImageUri(null))
        assertEquals(null, viewModel.state.value.imageUri)
    }
}
