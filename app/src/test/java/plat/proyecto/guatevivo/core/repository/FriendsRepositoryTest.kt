package plat.proyecto.guatevivo.core.repository

import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.testing.*

class FriendsRepositoryTest {
    private fun fake() = FakeFriendsFirebase().apply {
        store.profiles["me"] = visibleProfile("me", "Yo")
        store.profiles["other"] = visibleProfile("other", "Ana")
        store.emails["me@test.com"] = "me"
        store.emails["ana@test.com"] = "other"
    }
    private fun repo(fake: FakeFriendsFirebase) = FriendsRepository(fake) { fake.actor }
    private suspend fun rejected(block: suspend () -> Unit) {
        try { block(); fail("Expected rejected action") } catch (_: IllegalArgumentException) { } catch (_: IllegalStateException) { }
    }

    @Test fun emailNormalizationMatchesAuthConventionAndPreservesPlusAndDots() = runTest {
        val old = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals("i.name+tag@test.com", normalizeFriendEmail("  I.Name+TAG@TEST.COM  "))
        } finally { Locale.setDefault(old) }
        val fake = fake()
        assertEquals(EmailFriendRequestOutcome.SENT, repo(fake).send("  ANA@Test.COM  "))
        assertEquals(listOf("ana@test.com"), fake.sentEmails)
    }

    @Test fun invalidEmailIsRejectedBeforeAnyBackendCall() = runTest {
        val fake = fake()
        for (email in listOf("", "ana", "a@@test.com", "a @test.com", "ana@te st.com", ".a@test.com", "a..b@test.com", "a@test", "a@-test.com", "a@t.c", "x".repeat(65) + "@test.com"))
            rejected { repo(fake).send(email) }
        assertTrue(fake.sentEmails.isEmpty())
        assertTrue(fake.store.relationships.isEmpty())
    }

    @Test fun knowingEmailCanRequestHiddenAccountWithoutPublicSearch() = runTest {
        val fake = fake()
        assertFalse(fake.store.profiles.getValue("other").isDiscoverable)
        assertEquals(EmailFriendRequestOutcome.SENT, repo(fake).send("ana@test.com"))
        assertEquals("other", fake.store.relationships.values.single().receiverUid)
    }

    @Test fun selfRequestIsRejectedByResolvedUid() = runTest {
        val fake = fake()
        rejected { repo(fake).send("ME@test.com") }
        assertTrue(fake.store.relationships.isEmpty())
    }

    @Test fun pendingAndExistingFriendshipHaveExplicitOutcomesAndAreNotDuplicated() = runTest {
        val fake = fake()
        val repository = repo(fake)
        assertEquals(EmailFriendRequestOutcome.SENT, repository.send("ana@test.com"))
        assertEquals(EmailFriendRequestOutcome.PENDING, repository.send("ana@test.com"))
        fake.actor = "other"
        assertEquals(EmailFriendRequestOutcome.PENDING, repository.send("me@test.com"))
        repository.accept(fake.store.relationships.values.single().id)
        assertEquals(EmailFriendRequestOutcome.FRIENDS, repository.send("me@test.com"))
        assertEquals(1, fake.store.relationships.size)
    }

    @Test fun concurrentCrossedEmailsProduceOneRequest() = runTest {
        val first = fake()
        val second = FakeFriendsFirebase(first.store, "other")
        val outcomes = supervisorScope { listOf(async { repo(first).send("ana@test.com") }, async { repo(second).send("me@test.com") }).map { it.await() } }
        assertEquals(setOf(EmailFriendRequestOutcome.SENT, EmailFriendRequestOutcome.PENDING), outcomes.toSet())
        assertEquals(1, first.store.relationships.size)
    }

    @Test fun onlyReceiverCanAcceptAndOnlyPendingCanBeProcessed() = runTest {
        val fake = fake()
        val repository = repo(fake)
        repository.send("ana@test.com")
        val request = fake.store.relationships.values.single()
        rejected { repository.accept(request.id) }
        fake.actor = "intruder"
        rejected { repository.accept(request.id) }
        fake.actor = "other"
        repository.accept(request.id)
        rejected { repository.accept(request.id) }
        assertEquals("ACCEPTED", repository.load().relationships.single().status)
    }

    @Test fun rejectionAndResendKeepExistingRequestId() = runTest {
        val fake = fake()
        val repository = repo(fake)
        repository.send("ana@test.com")
        val request = fake.store.relationships.values.single()
        rejected { repository.reject(request.id) }
        fake.actor = "other"
        repository.reject(request.id)
        assertTrue(repository.load().relationships.isEmpty())
        fake.actor = "me"
        assertEquals(EmailFriendRequestOutcome.SENT, repository.send("ana@test.com"))
        assertEquals(request.id, fake.store.relationships.values.single().id)
    }

    @Test fun onlySenderCanCancelPendingAndAcceptedCannotBeCancelled() = runTest {
        val fake = fake()
        val repository = repo(fake)
        repository.send("ana@test.com")
        val id = fake.store.relationships.values.single().id
        fake.actor = "other"
        rejected { repository.cancel(id) }
        fake.actor = "me"
        repository.cancel(id)
        assertTrue(fake.store.relationships.isEmpty())
        fake.store.request("me", "other", "ACCEPTED")
        rejected { repository.cancel(id) }
    }

    @Test fun eitherFriendCanRemoveButThirdPartyCannot() = runTest {
        val fake = fake()
        fake.store.request("me", "other", "ACCEPTED")
        val repository = repo(fake)
        fake.actor = "intruder"
        rejected { repository.remove("other") }
        assertEquals(1, fake.store.relationships.size)
        fake.actor = "other"
        repository.remove("me")
        assertTrue(fake.store.relationships.isEmpty())
    }

    @Test fun privateContactsAreNotReturnedAndEvenDiscoverableProfilesRequireRelationship() = runTest {
        val user = UserDto(uid = "other", name = "Ana", email = "private@test.com", phone = "12345678", address = "Private address")
        val projection = user.publicProfile().toString()
        assertFalse(projection.contains(user.email))
        assertFalse(projection.contains(user.phone))
        assertFalse(projection.contains(user.address))
        val fake = fake()
        fake.store.profiles["other"] = user.publicProfile().copy(isDiscoverable = true)
        rejected { repo(fake).getProfile("other") }
        repo(fake).send("ana@test.com")
        assertEquals("other", repo(fake).getProfile("other")?.uid)
    }

    @Test fun deletedOrUnknownEmailProducesErrorNotSuccess() = runTest {
        val fake = fake()
        fake.store.profiles["other"] = fake.store.profiles.getValue("other").copy(isDeleted = true)
        for (email in listOf("ana@test.com", "unknown@test.com")) {
            try { repo(fake).send(email); fail("Expected unavailable account") }
            catch (error: FriendsActionException) { assertEquals("NOT_FOUND", error.code) }
        }
        assertTrue(fake.store.relationships.isEmpty())
    }

    @Test fun permissionsArePropagatedAndUnauthenticatedCallsNeverReachBackend() = runTest {
        val fake = fake().apply { mutationFailure = FriendsActionException("PERMISSION_DENIED", "Denied") }
        try { repo(fake).send("ana@test.com"); fail("Expected denied") }
        catch (error: FriendsActionException) { assertTrue(friendsFailureMessage(error).contains("PERMISSION_DENIED")) }
        assertTrue(fake.store.relationships.isEmpty())
        val before = fake.sendCalls
        val noSession = FriendsRepository(fake) { null }
        rejected { noSession.send("ana@test.com") }
        assertEquals(before, fake.sendCalls)
    }

    @Test fun stableIdsRemainCompatibleWithExistingFriendships() {
        assertEquals(128, friendRequestId("me", "other").length)
        assertNotEquals(friendRequestId("a_b", "c"), friendRequestId("a", "b_c"))
    }
}
