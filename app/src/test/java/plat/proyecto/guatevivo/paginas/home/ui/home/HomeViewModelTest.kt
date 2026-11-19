package plat.proyecto.guatevivo.paginas.home.ui.home

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
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val clock = Clock.fixed(Instant.parse("2026-11-19T18:00:00Z"), ZoneId.of("America/Guatemala"))
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun onlyPublishedPublicEventsAreShownWithoutDuplicates() = runTest {
        val repository = FakeEventRepository().apply {
            events = listOf(
                publicEvent("public"), publicEvent("public"),
                publicEvent("draft").copy(status = "DRAFT"),
                publicEvent("private").copy(visibility = "PRIVATE"),
                publicEvent("cancelled").copy(status = "CANCELLED"), publicEvent("")
            )
        }
        val viewModel = HomeViewModel(repository, clock)
        advanceUntilIdle()
        assertEquals(listOf("public"), viewModel.state.value.proximamenteItems.map { it.eventId })
        assertEquals("public", viewModel.state.value.destacadosItems.single().eventId)
    }

    @Test fun eventsAreChronologicalAndPastOrInvalidDatesAreExcluded() = runTest {
        val repository = FakeEventRepository().apply {
            events = listOf(
                publicEvent("next-year", "01/01/2027"), publicEvent("december", "01/12/2026"),
                publicEvent("evening", "19/11/2026", "20:00"), publicEvent("afternoon", "19/11/2026", "13:00"),
                publicEvent("past", "19/11/2026", "11:59"), publicEvent("invalid", "31/11/2026"),
                publicEvent("no-time").copy(hora = ""), publicEvent("no-date").copy(fecha = "")
            )
        }
        val viewModel = HomeViewModel(repository, clock)
        advanceUntilIdle()
        assertEquals(listOf("afternoon", "evening", "december", "next-year"), viewModel.state.value.proximamenteItems.map { it.eventId })
        assertEquals("19/11/2026", viewModel.state.value.proximamenteItems.first().fullDate)
        assertEquals("13:00", viewModel.state.value.proximamenteItems.first().hora)
    }

    @Test fun emptyCatalogueStaysEmpty() = runTest {
        val viewModel = HomeViewModel(FakeEventRepository(), clock)
        assertTrue(viewModel.state.value.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.proximamenteItems.isEmpty())
        assertTrue(viewModel.state.value.destacadosItems.isEmpty())
        assertNull(viewModel.state.value.error)
    }

    @Test fun refreshRemovesDeletedEventsAndResumeLoadsNewEvents() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(publicEvent("old")) }
        val viewModel = HomeViewModel(repository, clock)
        advanceUntilIdle()
        viewModel.onResume()
        assertEquals(1, repository.publicCalls)
        repository.events = listOf(publicEvent("new"))
        viewModel.onResume()
        advanceUntilIdle()
        assertEquals(listOf("new"), viewModel.state.value.proximamenteItems.map { it.eventId })
        repository.events = emptyList()
        viewModel.onEvent(HomeEvent.Refresh)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.proximamenteItems.isEmpty())
    }

    @Test fun concurrentRefreshesAreCoalescedAndQueryIsNotOverwritten() = runTest {
        val repository = FakeEventRepository().apply {
            events = listOf(publicEvent("a"), publicEvent("b"))
            gate = CompletableDeferred()
        }
        val viewModel = HomeViewModel(repository, clock)
        runCurrent()
        repeat(3) { viewModel.refresh() }
        viewModel.onEvent(HomeEvent.UpdateQuery("Evento b"))
        assertEquals(1, repository.publicCalls)
        repository.gate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals("Evento b", viewModel.state.value.searchQuery)
        assertEquals(listOf("b"), viewModel.state.value.proximamenteItems.map { it.eventId })
    }

    @Test fun localDataIsMarkedAndErrorsAllowRetry() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(publicEvent("cached")); isFromCache = true }
        val viewModel = HomeViewModel(repository, clock)
        advanceUntilIdle()
        assertTrue(viewModel.state.value.isFromCache)
        assertEquals(1, viewModel.state.value.proximamenteItems.size)
        repository.failure = IllegalStateException("failure")
        viewModel.refresh()
        advanceUntilIdle()
        assertNotNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
        assertTrue(viewModel.state.value.proximamenteItems.isEmpty())
        repository.failure = null
        repository.isFromCache = false
        viewModel.refresh()
        advanceUntilIdle()
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isFromCache)
        assertEquals(1, viewModel.state.value.proximamenteItems.size)
    }

    @Test fun cardIdLoadsTheSameEventInExistingDetail() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(publicEvent("original-firebase-id")) }
        val home = HomeViewModel(repository, clock)
        advanceUntilIdle()
        val destination = EventInfoDestination(home.state.value.proximamenteItems.single().eventId)
        assertNull(destination.mockEventId)
        val detail = EventInfoViewModel(destination, repository)
        advanceUntilIdle()
        assertEquals("original-firebase-id", repository.requestedId)
        assertEquals(repository.events.single().nombreEvento, detail.state.value.titulo)
        assertEquals("", detail.state.value.imageUrl)
        assertEquals(0, detail.state.value.imageResId)
    }

    @Test fun unavailableCategoryFiltersDoNotInventMatches() = runTest {
        val repository = FakeEventRepository().apply { events = listOf(publicEvent("a")) }
        val viewModel = HomeViewModel(repository, clock)
        advanceUntilIdle()
        viewModel.state.value.categories.forEach { category ->
            viewModel.onEvent(HomeEvent.SelectCategory(category))
            assertEquals("Todos", viewModel.state.value.selectedCategory)
            assertEquals(1, viewModel.state.value.proximamenteItems.size)
        }
        viewModel.onEvent(HomeEvent.UpdateQuery("missing"))
        assertTrue(viewModel.state.value.proximamenteItems.isEmpty())
        viewModel.onEvent(HomeEvent.ResetFilters)
        assertEquals(1, viewModel.state.value.proximamenteItems.size)
        assertEquals(1, repository.publicCalls)
    }
}
