package plat.proyecto.guatevivo.paginas.event.ui.info

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import plat.proyecto.guatevivo.testing.FakeEventRepository
import plat.proyecto.guatevivo.testing.publicEvent

@OptIn(ExperimentalCoroutinesApi::class)
class EventActionsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val event = publicEvent("real-id").copy(organizadorId = "owner", ubicacion = "Antigua")
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }
    private fun repository() = FakeEventRepository().apply { events = listOf(event) }
    private fun detail(repository: FakeEventRepository, uid: String = "owner") =
        EventInfoViewModel(EventInfoDestination(event.id), repository) { uid }

    @Test fun onlyOwnerCanManageAndOnlyPublicPublishedEventsCanBeShared() = runTest {
        val repository = repository()
        val owner = detail(repository)
        val visitor = detail(repository, "other")
        advanceUntilIdle()
        assertTrue(owner.state.value.canManage)
        assertTrue(owner.state.value.canShare)
        assertFalse(visitor.state.value.canManage)
        assertTrue(visitor.state.value.canShare)
        repository.events = listOf(event.copy(status = "DRAFT", visibility = "PRIVATE"))
        val draft = detail(repository)
        advanceUntilIdle()
        assertTrue(draft.state.value.canManage)
        assertFalse(draft.state.value.canShare)
    }

    @Test fun deleteRequiresConfirmationAndCanBeCancelled() = runTest {
        val repository = repository()
        val viewModel = detail(repository)
        advanceUntilIdle()
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        viewModel.onEvent(EventInfoEvent.RequestDelete)
        assertTrue(viewModel.state.value.showDeleteConfirmation)
        viewModel.onEvent(EventInfoEvent.CancelDelete)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showDeleteConfirmation)
        assertTrue(repository.deletedIds.isEmpty())
        assertEquals(1, repository.events.size)
    }

    @Test fun deleteCallsRepositoryOnceAndProducesConsumableNavigationState() = runTest {
        val repository = repository().apply { mutationGate = CompletableDeferred() }
        val viewModel = detail(repository)
        advanceUntilIdle()
        viewModel.onEvent(EventInfoEvent.RequestDelete)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        viewModel.onEvent(EventInfoEvent.CancelDelete)
        assertTrue(viewModel.state.value.isDeleting)
        assertTrue(viewModel.state.value.showDeleteConfirmation)
        repository.mutationGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf(event.id), repository.deletedIds)
        assertTrue(repository.events.isEmpty())
        assertTrue(viewModel.state.value.isDeleted)
        assertFalse(viewModel.state.value.canManage)
        viewModel.onEvent(EventInfoEvent.DeletionHandled)
        assertFalse(viewModel.state.value.isDeleted)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        advanceUntilIdle()
        assertEquals(1, repository.deletedIds.size)
    }

    @Test fun deleteErrorKeepsEventAndAllowsRetryOrCancel() = runTest {
        val repository = repository().apply { mutationFailure = IllegalStateException("Sin permiso") }
        val viewModel = detail(repository)
        advanceUntilIdle()
        viewModel.onEvent(EventInfoEvent.RequestDelete)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isDeleting)
        assertFalse(viewModel.state.value.isDeleted)
        assertTrue(viewModel.state.value.showDeleteConfirmation)
        assertEquals("Sin permiso", viewModel.state.value.error)
        assertEquals(1, repository.events.size)
        repository.mutationFailure = null
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isDeleted)
    }

    @Test fun visitorCannotTriggerDeletionEvenBySendingActionsDirectly() = runTest {
        val repository = repository()
        val viewModel = detail(repository, "other")
        advanceUntilIdle()
        viewModel.onEvent(EventInfoEvent.RequestDelete)
        viewModel.onEvent(EventInfoEvent.ConfirmDelete)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.showDeleteConfirmation)
        assertTrue(repository.deletedIds.isEmpty())
    }

    @Test fun sharingProducesOnlyOneTextEffectAndDoesNotInventAnEventLink() = runTest {
        val repository = repository()
        val viewModel = detail(repository)
        advanceUntilIdle()
        viewModel.onEvent(EventInfoEvent.Share)
        viewModel.onEvent(EventInfoEvent.Share)
        assertTrue(viewModel.state.value.isSharing)
        advanceUntilIdle()
        val text = viewModel.state.value.shareText!!
        assertTrue(text.contains(event.nombreEvento))
        assertTrue(text.contains("20/11/2026"))
        assertTrue(text.contains("18:30"))
        assertTrue(text.contains("Antigua"))
        assertFalse(text.contains("http"))
        viewModel.onEvent(EventInfoEvent.ShareHandled)
        assertNull(viewModel.state.value.shareText)
    }

    @Test fun changedVisibilityOrDeletedEventCannotBeSharedFromStaleDetail() = runTest {
        for (events in listOf(listOf(event.copy(status = "DRAFT", visibility = "PRIVATE")), emptyList())) {
            val repository = repository()
            val viewModel = detail(repository)
            advanceUntilIdle()
            repository.events = events
            viewModel.onEvent(EventInfoEvent.Share)
            advanceUntilIdle()
            assertNull(viewModel.state.value.shareText)
            assertNotNull(viewModel.state.value.error)
        }
    }

    @Test fun failedShareDoesNotExportCachedTextAndCanBeRetried() = runTest {
        val repository = repository()
        val viewModel = detail(repository)
        advanceUntilIdle()
        repository.failure = IllegalStateException("Sin conexión")
        viewModel.onEvent(EventInfoEvent.Share)
        advanceUntilIdle()
        assertNull(viewModel.state.value.shareText)
        assertFalse(viewModel.state.value.isSharing)
        assertEquals("Sin conexión", viewModel.state.value.error)
        repository.failure = null
        viewModel.onEvent(EventInfoEvent.Share)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.shareText)
    }

    @Test fun resumeReloadsEditedFieldsAndInvalidatesDeletedEvent() = runTest {
        val repository = repository()
        val viewModel = detail(repository)
        advanceUntilIdle()
        viewModel.onResume()
        repository.events = listOf(event.copy(nombreEvento = "Nombre editado"))
        viewModel.onResume()
        advanceUntilIdle()
        assertEquals("Nombre editado", viewModel.state.value.titulo)
        repository.events = emptyList()
        viewModel.onResume()
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canManage)
        assertFalse(viewModel.state.value.canShare)
        assertNotNull(viewModel.state.value.error)
    }

    @Test fun sharePolicyRejectsPrivateDraftCancelledAndInvalidDatesWithoutLinkSubstitution() {
        assertNull(event.copy(status = "DRAFT").publicShareText())
        assertNull(event.copy(visibility = "PRIVATE").publicShareText())
        assertNull(event.copy(status = "CANCELLED").publicShareText())
        assertNull(event.copy(fecha = "31/02/2027").publicShareText())
        assertNull(event.copy(hora = "24:00").publicShareText())
        val text = event.copy(imagenUrl = "https://storage.example.test/image", mapsUrl = "https://maps.example.test/place").publicShareText()!!
        assertFalse(text.contains("https://"))
    }
}
