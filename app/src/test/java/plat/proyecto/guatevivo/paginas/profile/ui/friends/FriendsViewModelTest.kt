package plat.proyecto.guatevivo.paginas.profile.ui.friends

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.repository.FriendsRepository
import plat.proyecto.guatevivo.testing.*

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }
    private fun fake() = FakeFriendsFirebase().apply {
        store.profiles["me"] = visibleProfile("me", "Yo")
        store.profiles["ana"] = visibleProfile("ana", "Ana")
        store.profiles["beto"] = visibleProfile("beto", "Beto")
        store.emails["me@test.com"] = "me"
        store.emails["ana@test.com"] = "ana"
        store.emails["beto@test.com"] = "beto"
    }
    private fun viewModel(
        fake: FakeFriendsFirebase,
        uid: String? = null,
        accountType: suspend () -> String? = { "USER" }
    ) = FriendsViewModel(FriendsRepository(fake, { fake.actor }, accountType), uid)

    @Test fun businessAccountCannotOpenOrSendFriendsFromViewModel() = runTest {
        val fake = fake()
        val vm = viewModel(fake, accountType = { "BUSINESS" })
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.sendOutcome)
        assertTrue(fake.sentEmails.isEmpty())
    }

    @Test fun typingEmailDoesNotQueryOrSendAndInvalidFormatIsRejected() = runTest {
        val fake = fake()
        val vm = viewModel(fake)
        advanceUntilIdle()
        for (value in listOf("a", "ana@", "ana @test.com")) {
            vm.onEvent(FriendsEvent.UpdateEmail(value))
            advanceUntilIdle()
        }
        assertTrue(fake.sentEmails.isEmpty())
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertNotNull(vm.state.value.sendError)
        assertNull(vm.state.value.sendOutcome)
        assertTrue(fake.sentEmails.isEmpty())
        assertTrue(vm.state.value.friends.isEmpty())
    }

    @Test fun sendingNormalizesEmailBlocksDoubleClicksAndReloadsRealPendingRequest() = runTest {
        val fake = fake().apply { mutationGate = CompletableDeferred() }
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("  ANA@Test.COM "))
        vm.onEvent(FriendsEvent.SendRequest)
        vm.onEvent(FriendsEvent.SendRequest)
        assertTrue(vm.state.value.isSending)
        vm.onEvent(FriendsEvent.UpdateEmail("changed@test.com"))
        fake.mutationGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(listOf("ana@test.com"), fake.sentEmails)
        assertEquals("ana@test.com", vm.state.value.email)
        assertEquals(EmailFriendRequestOutcome.SENT, vm.state.value.sendOutcome)
        assertEquals("ana", vm.state.value.requests.single().receiverUid)
        assertFalse(vm.state.value.isSending)
    }

    @Test fun pendingAndFriendshipOutcomesAreExplicitAndDoNotClaimNewRequests() = runTest {
        val fake = fake()
        val request = fake.store.request("ana", "me")
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertEquals(EmailFriendRequestOutcome.PENDING, vm.state.value.sendOutcome)
        vm.onEvent(FriendsEvent.Accept(request.id))
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertEquals(EmailFriendRequestOutcome.FRIENDS, vm.state.value.sendOutcome)
        assertEquals(1, fake.store.relationships.size)
    }

    @Test fun permissionDeniedIsVisibleAndNeverConvertedToSuccess() = runTest {
        val fake = fake().apply { mutationFailure = FriendsActionException("PERMISSION_DENIED", "Denied") }
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertTrue(vm.state.value.sendError!!.contains("PERMISSION_DENIED"))
        assertNull(vm.state.value.sendOutcome)
        assertFalse(vm.state.value.isSending)
        assertTrue(fake.store.relationships.isEmpty())
        fake.mutationFailure = null
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertEquals(EmailFriendRequestOutcome.SENT, vm.state.value.sendOutcome)
    }

    @Test fun postSendReadPermissionFailureIsNotHiddenBySuccessMessage() = runTest {
        val fake = fake().apply { readFailureAfterSend = FriendsActionException("PERMISSION_DENIED", "Denied") }
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertEquals(1, fake.store.relationships.size)
        assertNull(vm.state.value.sendOutcome)
        assertTrue(vm.state.value.sendError!!.contains("PERMISSION_DENIED"))
        fake.failure = null
        fake.readFailureAfterSend = null
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertEquals(EmailFriendRequestOutcome.PENDING, vm.state.value.sendOutcome)
        assertEquals(1, fake.store.relationships.size)
    }

    @Test fun missingBackendAndUnknownEmailDoNotSimulateSuccess() = runTest {
        val fake = fake()
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.UpdateEmail("missing@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertNull(vm.state.value.sendOutcome)
        assertNotNull(vm.state.value.sendError)
        fake.mutationFailure = FriendsActionException("FAILED_PRECONDITION", "Configura la función")
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        assertTrue(vm.state.value.sendError!!.contains("FAILED_PRECONDITION"))
        assertNull(vm.state.value.sendOutcome)
    }

    @Test fun acceptanceRejectionAndCancellationRemainFunctionalAndContextual() = runTest {
        val fake = fake()
        val incoming = fake.store.request("ana", "me")
        val outgoing = fake.store.request("me", "beto")
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.Accept(outgoing.id))
        vm.onEvent(FriendsEvent.Cancel(incoming.id))
        advanceUntilIdle()
        assertEquals(2, vm.state.value.requests.size)
        vm.onEvent(FriendsEvent.Reject(incoming.id))
        advanceUntilIdle()
        assertEquals("REJECTED", fake.store.relationships.getValue(incoming.id).status)
        vm.onEvent(FriendsEvent.Cancel(outgoing.id))
        advanceUntilIdle()
        assertTrue(vm.state.value.requests.isEmpty())
        val accepted = fake.store.request("ana", "me")
        vm.onEvent(FriendsEvent.Refresh)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.Accept(accepted.id))
        advanceUntilIdle()
        assertEquals(1, vm.state.value.friends.size)
    }

    @Test fun removingFriendStillRequiresConfirmationAndBlocksDuplicates() = runTest {
        val fake = fake().apply { mutationGate = CompletableDeferred() }
        fake.store.request("me", "ana", "ACCEPTED")
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onEvent(FriendsEvent.ConfirmRemove)
        vm.onEvent(FriendsEvent.Remove("ana"))
        vm.onEvent(FriendsEvent.CancelRemove)
        assertEquals(0, fake.removeCalls)
        vm.onEvent(FriendsEvent.Remove("ana"))
        vm.onEvent(FriendsEvent.ConfirmRemove)
        vm.onEvent(FriendsEvent.ConfirmRemove)
        fake.mutationGate!!.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, fake.removeCalls)
        assertTrue(vm.state.value.friends.isEmpty())
    }

    @Test fun refreshPreservesFormAndTabWithoutResendingOrRepeatingSuccessEffects() = runTest {
        val fake = fake()
        val vm = viewModel(fake)
        advanceUntilIdle()
        vm.onResume()
        vm.onEvent(FriendsEvent.SelectTab(1))
        vm.onEvent(FriendsEvent.UpdateEmail("ana@test.com"))
        vm.onEvent(FriendsEvent.SendRequest)
        advanceUntilIdle()
        fake.store.request("beto", "me")
        vm.onResume()
        advanceUntilIdle()
        assertEquals("ana@test.com", vm.state.value.email)
        assertEquals(1, vm.state.value.selectedTab)
        assertEquals(1, fake.sendCalls)
        assertEquals(2, vm.state.value.requests.size)
        vm.onEvent(FriendsEvent.UpdateEmail(""))
        assertNull(vm.state.value.sendOutcome)
    }

    @Test fun existingProfilesOpenOnlyForAuthorizedRelationships() = runTest {
        val fake = fake()
        fake.store.request("me", "ana", "ACCEPTED")
        val vm = viewModel(fake, "ana")
        advanceUntilIdle()
        assertEquals("ana", vm.state.value.openedProfile?.uid)
        val unrelated = viewModel(fake, "beto")
        advanceUntilIdle()
        assertNull(unrelated.state.value.openedProfile)
        assertTrue(unrelated.state.value.error!!.contains("PERMISSION_DENIED"))
    }

    @Test fun initialPermissionErrorIsVisibleAndRetryDoesNotInventData() = runTest {
        val fake = fake().apply { failure = FriendsActionException("PERMISSION_DENIED", "Denied") }
        val vm = viewModel(fake)
        advanceUntilIdle()
        assertTrue(vm.state.value.error!!.contains("PERMISSION_DENIED"))
        assertTrue(vm.state.value.friends.isEmpty())
        fake.failure = null
        vm.onEvent(FriendsEvent.Refresh)
        advanceUntilIdle()
        assertNull(vm.state.value.error)
        assertEquals("me", vm.state.value.uid)
    }
}
