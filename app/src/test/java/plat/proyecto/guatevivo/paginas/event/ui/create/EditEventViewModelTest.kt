package plat.proyecto.guatevivo.paginas.event.ui.create

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
class EditEventViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val original = publicEvent("real-id").copy(
        organizadorId = "owner", createdAt = 123456L, descripcion = "Descripción completa",
        ubicacion = "Antigua", incluirConfirmacion = true,
        imagenUrl = "https://storage.example.test/original.jpg", mapsUrl = "https://maps.example.test/place"
    )
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }
    private fun repository() = FakeEventRepository().apply { events = listOf(original) }
    private fun editor(repository: FakeEventRepository) = CreateEventViewModel(repository, original.id) { "owner" }

    @Test fun editingLoadsOriginalIdAndPrefillsEveryFormField() = runTest {
        val repository = repository()
        val viewModel = editor(repository)
        assertTrue(viewModel.state.value.isLoading)
        assertFalse(viewModel.state.value.canSave)
        advanceUntilIdle()
        val state = viewModel.state.value
        assertEquals(original.id, repository.requestedId)
        assertEquals(original.id, state.eventId)
        assertTrue(state.isEditing)
        assertTrue(state.canSave)
        assertEquals(original.nombreEvento, state.nombreEvento)
        assertEquals(original.ubicacion, state.ubicacion)
        assertEquals(original.fecha, state.fecha)
        assertEquals(original.hora, state.hora)
        assertEquals(original.descripcion, state.descripcion)
        assertEquals(original.imagenUrl, state.imageUri)
        assertEquals(original.incluirConfirmacion, state.incluirConfirmacion)
    }

    @Test fun publishedEditUpdatesSameDocumentAndPreservesProtectedMetadata() = runTest {
        val repository = repository()
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateNombreEvento(" Nombre nuevo "))
        viewModel.onEvent(CreateEventEvent.UpdateIncluirConfirmacion(false))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertEquals(original.copy(nombreEvento = "Nombre nuevo", incluirConfirmacion = false), repository.updatedEvents.single())
        assertTrue(repository.createdDrafts.isEmpty())
        assertTrue(repository.updatedDrafts.isEmpty())
        assertTrue(repository.imageUploads.isEmpty())
        assertTrue(viewModel.state.value.isSuccess)
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals(1, repository.updatedEvents.size)
        viewModel.onEvent(CreateEventEvent.ClearSuccess)
        assertFalse(viewModel.state.value.isSuccess)
        assertEquals(" Nombre nuevo ", viewModel.state.value.nombreEvento)
    }

    @Test fun draftEditUsesUpdateDraftAndDoesNotCreateOrPublishAnotherEvent() = runTest {
        val draft = original.copy(status = "DRAFT", visibility = "PRIVATE")
        val repository = repository().apply { events = listOf(draft) }
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateDescripcion("Otra descripción"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals(draft.copy(descripcion = "Otra descripción"), repository.updatedDrafts.single())
        assertTrue(repository.updatedEvents.isEmpty())
        assertTrue(repository.createdDrafts.isEmpty())
    }

    @Test fun newImageIsUploadedForSameIdAndRemoteUrlIsSaved() = runTest {
        val repository = repository()
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateImageUri("content://photos/selected"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals(listOf(original.id to "content://photos/selected"), repository.imageUploads)
        assertEquals(repository.uploadedImageUrl, repository.updatedEvents.single().imagenUrl)
        assertEquals(original.createdAt, repository.updatedEvents.single().createdAt)
    }

    @Test fun removeImageSavesEmptyUrlWithoutUploadingOrDeletingArbitraryStorageObjects() = runTest {
        val repository = repository()
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateImageUri(null))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals("", repository.updatedEvents.single().imagenUrl)
        assertTrue(repository.imageUploads.isEmpty())
    }

    @Test fun uploadFailureKeepsOriginalEventAndAllowsRetry() = runTest {
        val repository = repository().apply { uploadFailure = IllegalStateException("No se pudo subir") }
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateImageUri("content://photos/selected"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals("No se pudo subir", viewModel.state.value.error)
        assertEquals(original, repository.events.single())
        assertTrue(repository.updatedEvents.isEmpty())
        assertFalse(viewModel.state.value.isSuccess)
        repository.uploadFailure = null
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isSuccess)
    }

    @Test fun failedSavePreservesFormAndPreventsRepeatedWritesWhilePending() = runTest {
        val repository = repository().apply {
            mutationFailure = IllegalStateException("Sin permiso")
            mutationGate = CompletableDeferred()
        }
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateNombreEvento("Mi cambio"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        repeat(3) { viewModel.onEvent(CreateEventEvent.CreateEvent) }
        assertTrue(viewModel.state.value.isLoading)
        repository.mutationGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals("Sin permiso", viewModel.state.value.error)
        assertEquals("Mi cambio", viewModel.state.value.nombreEvento)
        assertFalse(viewModel.state.value.isSuccess)
        assertEquals(original, repository.events.single())
        repository.mutationFailure = null
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals(1, repository.updatedEvents.size)
    }

    @Test fun invalidDateOrTimeCannotUpdateEvent() = runTest {
        val repository = repository()
        val viewModel = editor(repository)
        advanceUntilIdle()
        viewModel.onEvent(CreateEventEvent.UpdateFecha("31/02/2027"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        viewModel.onEvent(CreateEventEvent.UpdateFecha("21/02/2027"))
        viewModel.onEvent(CreateEventEvent.UpdateHora("24:00"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertTrue(repository.updatedEvents.isEmpty())
    }

    @Test fun foreignOrMissingEventCannotBeEditedOrAccidentallyCreated() = runTest {
        for (events in listOf(listOf(original.copy(organizadorId = "other")), emptyList())) {
            val repository = repository().apply { this.events = events }
            val viewModel = editor(repository)
            advanceUntilIdle()
            assertFalse(viewModel.state.value.canSave)
            assertNotNull(viewModel.state.value.error)
            viewModel.onEvent(CreateEventEvent.CreateEvent)
            advanceUntilIdle()
            assertTrue(repository.updatedEvents.isEmpty())
            assertTrue(repository.createdDrafts.isEmpty())
        }
    }

    @Test fun failedLoadCanBeRetriedWithSameRealId() = runTest {
        val repository = repository().apply { failure = IllegalStateException("Sin conexión") }
        val viewModel = editor(repository)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canSave)
        repository.failure = null
        viewModel.onEvent(CreateEventEvent.RetryLoad)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.canSave)
        assertEquals(original.id, repository.requestedId)
    }

    @Test fun sessionChangeAfterLoadingPreventsSavingAnotherOwnersEvent() = runTest {
        val repository = repository()
        var uid = "owner"
        val viewModel = CreateEventViewModel(repository, original.id) { uid }
        advanceUntilIdle()
        uid = "other"
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertTrue(repository.updatedEvents.isEmpty())
    }

    @Test fun newEventStillUsesDraftCreation() = runTest {
        val repository = repository()
        val viewModel = CreateEventViewModel(repository, currentUserId = { "owner" })
        viewModel.onEvent(CreateEventEvent.UpdateNombreEvento("Nuevo"))
        viewModel.onEvent(CreateEventEvent.UpdateFecha("21/02/2027"))
        viewModel.onEvent(CreateEventEvent.UpdateHora("14:30"))
        viewModel.onEvent(CreateEventEvent.CreateEvent)
        advanceUntilIdle()
        assertEquals("owner", repository.createdDrafts.single().organizadorId)
        assertEquals("DRAFT", repository.createdDrafts.single().status)
        assertTrue(repository.updatedEvents.isEmpty())
    }
}
