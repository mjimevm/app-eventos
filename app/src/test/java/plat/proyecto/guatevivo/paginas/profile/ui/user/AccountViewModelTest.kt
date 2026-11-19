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
        override suspend fun getEventById(eventId: String): EventDto? = null
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = emptyList()
        override suspend fun updateEvent(event: EventDto) {}
        override suspend fun deleteEvent(eventId: String) {}
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
}
