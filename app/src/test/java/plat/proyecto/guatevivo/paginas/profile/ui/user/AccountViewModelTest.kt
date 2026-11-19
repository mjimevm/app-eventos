package plat.proyecto.guatevivo.paginas.profile.ui.user

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
import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.UserRepository
import plat.proyecto.guatevivo.core.repository.AttendanceRepository
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthFirebase : AuthFirebase {
        override suspend fun register(email: String, password: String): String = ""
        override suspend fun login(email: String, password: String): String = ""
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String = "user_123"
        override fun getCurrentUserEmail(): String = "user@guatevivo.com"
        override fun isLoggedIn(): Boolean = true
        override fun logout() {}
    }

    private class FakeUserFirebase : UserFirebase {
        override suspend fun createUser(user: UserDto) {}
        override suspend fun getUser(uid: String): UserDto = UserDto(uid = uid, name = "Test User", email = "user@guatevivo.com")
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    private class FakeEventRepository : EventRepository {
        override suspend fun createDraft(event: EventDto, imageUri: String?): String = "draft_1"
        override suspend fun updateDraft(event: EventDto) {}
        override suspend fun publishEvent(eventId: String) {}
        override suspend fun getPublicEvents(): List<EventDto> = emptyList()
        override suspend fun getMyDrafts(): List<EventDto> = listOf(
            EventDto(
                id = "draft_1",
                nombreEvento = "Borrador Test",
                organizadorId = "user_123",
                status = EventStatus.DRAFT.name,
                visibility = EventVisibility.PRIVATE.name
            )
        )
        override suspend fun getMyCreatedEvents(): List<EventDto> = emptyList()
        override suspend fun getEventById(eventId: String): EventDto? =
            if (eventId == "attending_1") EventDto(
                id = "attending_1",
                nombreEvento = "Evento asistido",
                fecha = "20/11/2026",
                hora = "18:00",
                ubicacion = "Antigua",
                incluirConfirmacion = true,
                organizadorId = "other",
                status = EventStatus.PUBLISHED.name
            ) else null
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = emptyList()
        override suspend fun updateEvent(event: EventDto) {}
        override suspend fun deleteEvent(eventId: String) {}
    }

    private class FakeAttendanceFirebase : AttendanceFirebase {
        var attending = true
        var failure: Exception? = null
        override suspend fun confirmAttendance(eventId: String) { attending = true }
        override suspend fun cancelAttendance(eventId: String) { attending = false }
        override suspend fun isAttending(eventId: String): Boolean = attending
        override suspend fun getTotalAttendees(eventId: String): Int = if (attending) 1 else 0
        override suspend fun getAttendingFriendsCount(eventId: String): Int = 0
        override suspend fun getAttendanceStats(eventId: String): AttendanceStatsDto =
            AttendanceStatsDto(if (attending) 1 else 0, 0, attending)
        override suspend fun getAttendingEventIds(): List<String> {
            failure?.let { throw it }
            return if (attending) listOf("attending_1") else emptyList()
        }
    }

    @Test
    fun loadUserProfile_success() = runTest {
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())
        val userRepo = UserRepository(FakeUserFirebase())
        val eventRepo = FakeEventRepository()
        val viewModel = AccountViewModel(authRepo, userRepo, eventRepo)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Test User", state.usuario)
        assertEquals("user@guatevivo.com", state.correo)
        assertEquals(1, state.listaCreados.size)
        assertEquals("Borrador Test", state.listaCreados[0].title)
        assertFalse(state.isLoading)
    }

    @Test
    fun attendingEvents_restoreInProfileAndCancelRemovesThem() = runTest {
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())
        val attendance = FakeAttendanceFirebase()
        val viewModel = AccountViewModel(
            authRepo,
            UserRepository(FakeUserFirebase()),
            FakeEventRepository(),
            AttendanceRepository(attendance)
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.state.value.listaAsistidos.size)
        assertEquals("Evento asistido", viewModel.state.value.listaAsistidos.single().title)

        attendance.attending = false
        viewModel.loadData()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.listaAsistidos.isEmpty())
        assertEquals(0, viewModel.state.value.listaAsistidos.size)
    }

    @Test
    fun attendingLoadFailure_isReportedWithoutMockData() = runTest {
        val attendance = FakeAttendanceFirebase().apply {
            failure = IllegalStateException("Firestore no disponible")
        }
        val viewModel = AccountViewModel(
            AuthRepository(FakeAuthFirebase(), FakeUserFirebase()),
            UserRepository(FakeUserFirebase()),
            FakeEventRepository(),
            AttendanceRepository(attendance)
        )
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.state.value.error!!.contains("Firestore no disponible"))
        assertTrue(viewModel.state.value.listaAsistidos.isEmpty())
    }
}
