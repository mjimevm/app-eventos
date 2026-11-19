package plat.proyecto.guatevivo.paginas.profile.ui.bussines

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
class BusinessViewModelTest {

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
        override fun getCurrentUserId(): String = "biz_123"
        override fun getCurrentUserEmail(): String = "biz@guatevivo.com"
        override fun isLoggedIn(): Boolean = true
        override fun logout() {}
    }

    private class FakeUserFirebase : UserFirebase {
        override suspend fun createUser(user: UserDto) {}
        override suspend fun getUser(uid: String): UserDto = UserDto(uid = uid, name = "Guate Business", email = "biz@guatevivo.com")
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    private class FakeEventRepository : EventRepository {
        var failure: Exception? = null
        var publishCalls = 0
        var deleteCalls = 0
        private val published = mutableListOf<EventDto>()
        private val drafts = mutableListOf(
            EventDto(
                id = "draft_1",
                nombreEvento = "Borrador Empresa",
                fecha = "19/11/2026",
                organizadorId = "biz_123",
                status = EventStatus.DRAFT.name,
                visibility = EventVisibility.PRIVATE.name
            )
        )

        override suspend fun createDraft(event: EventDto, imageUri: String?): String {
            val newId = "draft_${drafts.size + 1}"
            drafts.add(event.copy(id = newId, status = EventStatus.DRAFT.name))
            return newId
        }

        override suspend fun updateDraft(event: EventDto) {}
        override suspend fun publishEvent(eventId: String) {
            publishCalls++
            failure?.let { throw it }
            val event = drafts.first { it.id == eventId }
            drafts.remove(event)
            published.add(event.copy(status = EventStatus.PUBLISHED.name, visibility = EventVisibility.PUBLIC.name))
        }
        override suspend fun getPublicEvents(): List<EventDto> = emptyList()
        override suspend fun getMyDrafts(): List<EventDto> = drafts
        override suspend fun getMyCreatedEvents(): List<EventDto> = drafts + published
        override suspend fun getEventById(eventId: String): EventDto? = null
        override suspend fun getEventsByOrganizer(organizerId: String): List<EventDto> = emptyList()
        override suspend fun updateEvent(event: EventDto) {}
        override suspend fun deleteEvent(eventId: String) {
            deleteCalls++
            failure?.let { throw it }
            drafts.removeAll { it.id == eventId }
        }
    }

    @Test
    fun loadBusinessProfile_success() = runTest {
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())
        val userRepo = UserRepository(FakeUserFirebase())
        val eventRepo = FakeEventRepository()
        val viewModel = BusinessViewModel(authRepo, userRepo, eventRepo)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Guate Business", state.empresaNombre)
        assertEquals(1, state.listaCreados.size)
        assertEquals("Borrador Empresa", state.listaCreados[0].title)
        assertFalse(state.isLoading)
    }

    @Test
    fun refresh_updatesDraftsListAfterNewDraftCreated() = runTest {
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())
        val userRepo = UserRepository(FakeUserFirebase())
        val eventRepo = FakeEventRepository()
        val viewModel = BusinessViewModel(authRepo, userRepo, eventRepo)

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.state.value.listaCreados.size)

        // Simular la creación de un nuevo borrador
        eventRepo.createDraft(
            EventDto(
                nombreEvento = "Nuevo Borrador Creado",
                organizadorId = "biz_123",
                status = EventStatus.DRAFT.name,
                visibility = EventVisibility.PRIVATE.name
            )
        )

        // Simular la actualización al regresar al perfil empresarial
        viewModel.onEvent(BusinessEvent.Refresh)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(2, state.listaCreados.size)
        assertEquals("Nuevo Borrador Creado", state.listaCreados[1].title)
    }
    private fun createViewModel(repository: EventRepository) = BusinessViewModel(
        AuthRepository(FakeAuthFirebase(), FakeUserFirebase()),
        UserRepository(FakeUserFirebase()), repository
    )

    @Test
    fun publishDraft_movesCardToPublishedAndPreservesRealId() = runTest {
        val repository = FakeEventRepository()
        val viewModel = createViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("draft_1", viewModel.state.value.listaCreados.single().eventId)
        assertEquals("NOV", viewModel.state.value.listaCreados.single().fecha)
        assertEquals(19, viewModel.state.value.listaCreados.single().dia)

        viewModel.onEvent(BusinessEvent.PublishDraft("draft_1"))
        assertEquals("draft_1", viewModel.state.value.processingEventId)
        viewModel.onEvent(BusinessEvent.PublishDraft("draft_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.publishCalls)
        assertTrue(viewModel.state.value.listaCreados.isEmpty())
        assertEquals("draft_1", viewModel.state.value.listaPublicados.single().eventId)
        assertNull(viewModel.state.value.processingEventId)
    }

    @Test
    fun deleteDraft_removesCardAndRejectsDuplicateOperation() = runTest {
        val repository = FakeEventRepository()
        val viewModel = createViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEvent(BusinessEvent.DeleteDraft("draft_1"))
        viewModel.onEvent(BusinessEvent.DeleteDraft("draft_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, repository.deleteCalls)
        assertTrue(viewModel.state.value.listaCreados.isEmpty())
        assertTrue(viewModel.state.value.listaPublicados.isEmpty())
        assertNull(viewModel.state.value.processingEventId)
    }

    @Test
    fun failedMutations_keepDraftAndAllowRetry() = runTest {
        val repository = FakeEventRepository()
        val viewModel = createViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        repository.failure = IllegalStateException("Sin conexion")

        for (event in listOf(BusinessEvent.PublishDraft("draft_1"), BusinessEvent.DeleteDraft("draft_1"))) {
            viewModel.onEvent(event)
            testDispatcher.scheduler.advanceUntilIdle()
            assertEquals("Sin conexion", viewModel.state.value.error)
            assertEquals(1, viewModel.state.value.listaCreados.size)
            assertNull(viewModel.state.value.processingEventId)
            viewModel.onEvent(BusinessEvent.ClearError)
            assertNull(viewModel.state.value.error)
        }
        repository.failure = null
        viewModel.onEvent(BusinessEvent.PublishDraft("draft_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.state.value.listaPublicados.size)
    }

    @Test
    fun actionsOnUnknownOrPublishedEvents_doNotCallRepository() = runTest {
        val repository = FakeEventRepository()
        val viewModel = createViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(BusinessEvent.DeleteDraft("unknown"))
        viewModel.onEvent(BusinessEvent.PublishDraft(""))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, repository.deleteCalls)
        assertEquals(0, repository.publishCalls)
        viewModel.onEvent(BusinessEvent.PublishDraft("draft_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.onEvent(BusinessEvent.DeleteDraft("draft_1"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(0, repository.deleteCalls)
    }

}
