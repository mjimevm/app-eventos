package plat.proyecto.guatevivo.paginas.home.ui.search

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import plat.proyecto.guatevivo.paginas.event.ui.info.EventInfoDestination
import plat.proyecto.guatevivo.paginas.event.ui.info.EventInfoViewModel
import plat.proyecto.guatevivo.testing.FakeEventRepository
import plat.proyecto.guatevivo.testing.publicEvent

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneId.of("America/Guatemala"))
    private val flowers = publicEvent("flowers").copy(
        nombreEvento = "Festival de las Flóres", ubicacion = "Antigua   Guatemala", descripcion = "Arte y música colonial"
    )
    private val food = publicEvent("food", "01/12/2026").copy(
        nombreEvento = "Feria gastronómica", ubicacion = "Quetzaltenango", descripcion = "Sabores tradicionales"
    )
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun searchesNameLocationAndDescriptionIgnoringCaseAccentsAndSpaces() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(food, flowers) }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        for (query in listOf("flores", "FESTIVAL", "  ANTiGUA    Guatemála  ", "MUSICA colonial", "flores antigua")) {
            viewModel.onEvent(SearchEvent.UpdateQuery(query))
            assertEquals(query, listOf("flowers"), viewModel.state.value.searchResults.map { it.eventId })
        }
        viewModel.onEvent(SearchEvent.UpdateQuery("GASTRONOMICA"))
        assertEquals("food", viewModel.state.value.searchResults.single().eventId)
        assertEquals(1, repository.publicCalls)
    }

    @Test fun noMatchesAndClearQueryRestoreChronologicalResults() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(food, flowers) }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        viewModel.onEvent(SearchEvent.UpdateQuery("no existe"))
        assertTrue(viewModel.state.value.searchResults.isEmpty())
        viewModel.onEvent(SearchEvent.UpdateQuery(""))
        assertEquals(listOf("flowers", "food"), viewModel.state.value.searchResults.map { it.eventId })
        assertEquals(1, repository.publicCalls)
    }

    @Test fun unsupportedFiltersCannotFabricateCategoryOrPopularityAndResetRestoresResults() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(food, flowers) }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        // Each existing option lacks a backing property. Applying one must never infer categories.
        viewModel.state.value.categories.forEach { category ->
            viewModel.onEvent(SearchEvent.SelectCategory(category))
            assertEquals("", viewModel.state.value.selectedCategory)
            assertEquals(2, viewModel.state.value.searchResults.size)
            viewModel.onEvent(SearchEvent.UpdateQuery("antigua"))
            viewModel.onEvent(SearchEvent.SelectCategory(category))
            assertEquals("flowers", viewModel.state.value.searchResults.single().eventId)
            viewModel.onEvent(SearchEvent.ResetFilters)
            assertEquals("", viewModel.state.value.searchQuery)
            assertEquals(2, viewModel.state.value.searchResults.size)
        }
        assertEquals(1, repository.publicCalls)
    }

    @Test fun onlyUpcomingPublishedPublicEventsAreSearchable() = runTest {
        val repository = FakeEventRepository().apply {
            events = listOf(
                flowers, flowers, food.copy(status = "DRAFT"), food.copy(id = "private", visibility = "PRIVATE"),
                food.copy(id = "cancelled", status = "CANCELLED"), publicEvent("past", "18/11/2026")
            )
        }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        assertEquals(listOf("flowers"), viewModel.state.value.searchResults.map { it.eventId })
        viewModel.onEvent(SearchEvent.UpdateQuery("gastronomica"))
        assertTrue(viewModel.state.value.searchResults.isEmpty())
    }

    @Test fun resumeRefreshPreservesQueryAndRemovesDeletedEvents() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(food, flowers) }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        viewModel.onResume()
        assertEquals(1, repository.publicCalls)
        viewModel.onEvent(SearchEvent.UpdateQuery("antigua"))
        repository.events = listOf(food)
        viewModel.onResume()
        advanceUntilIdle()
        assertEquals("antigua", viewModel.state.value.searchQuery)
        assertTrue(viewModel.state.value.searchResults.isEmpty())
        viewModel.onEvent(SearchEvent.ResetFilters)
        assertEquals(listOf("food"), viewModel.state.value.searchResults.map { it.eventId })
    }

    @Test fun overlappingRefreshesDoNotOverwriteLatestQuery() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(food, flowers); gate = CompletableDeferred() }
        val viewModel = SearchViewModel(repository, clock)
        runCurrent()
        repeat(4) { viewModel.refresh() }
        viewModel.onEvent(SearchEvent.UpdateQuery("sabores"))
        repository.gate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, repository.publicCalls)
        assertEquals("food", viewModel.state.value.searchResults.single().eventId)
    }

    @Test fun localCacheIsMarkedAndLoadFailureCanBeRetried() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(flowers); isFromCache = true }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isFromCache)
        assertEquals(1, viewModel.state.value.searchResults.size)
        repository.failure = IllegalStateException("error")
        viewModel.refresh()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.searchResults.isEmpty())
        repository.failure = null
        repository.events = emptyList()
        repository.isFromCache = false
        viewModel.onEvent(SearchEvent.Refresh)
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isFromCache)
        assertTrue(viewModel.state.value.searchResults.isEmpty())
    }

    @Test fun searchCardOpensExistingDetailWithRealId() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(flowers) }
        val viewModel = SearchViewModel(repository, clock)
        advanceUntilIdle()
        val route = EventInfoDestination(viewModel.state.value.searchResults.single().eventId)
        assertNull(route.mockEventId)
        val detail = EventInfoViewModel(route, repository)
        advanceUntilIdle()
        assertEquals("flowers", repository.requestedId)
        assertEquals(flowers.nombreEvento, detail.state.value.titulo)
    }
}
