package plat.proyecto.guatevivo.core.firebase.funciones.amigos

import org.junit.Assert.*
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.configuredFriendEndpoint
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.parseEmailFriendResponse
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.*

class EmailFriendRequestClientTest {
    @Test fun onlyExplicitConfirmedOutcomesAreAccepted() {
        EmailFriendRequestOutcome.entries.forEach {
            assertEquals(it, parseEmailFriendResponse(200, """{"result":{"outcome":"${it.name}"}}"""))
        }
        for (body in listOf("", "{}", "<html>Not found</html>", """{"result":{"outcome":"OK"}}""")) {
            try { parseEmailFriendResponse(200, body); fail("Expected unconfirmed response error") }
            catch (error: FriendsActionException) { assertEquals("INVALID_RESPONSE", error.code) }
        }
    }

    @Test fun permissionDeniedIncludingIamHtmlIsNeverSuccess() {
        for (body in listOf("<html>Forbidden</html>", """{"error":{"status":"PERMISSION_DENIED","message":"Denied"}}""")) {
            try { parseEmailFriendResponse(403, body); fail("Expected permission error") }
            catch (error: FriendsActionException) { assertEquals("PERMISSION_DENIED", error.code) }
        }
    }

    @Test fun backendErrorAndMissingDeploymentHaveExplicitCodes() {
        for ((status, code) in listOf(401 to "UNAUTHENTICATED", 404 to "NOT_FOUND", 429 to "RESOURCE_EXHAUSTED")) {
            try { parseEmailFriendResponse(status, ""); fail("Expected backend error") }
            catch (error: FriendsActionException) { assertEquals(code, error.code) }
        }
        try { parseEmailFriendResponse(200, """{"error":{"status":"PERMISSION_DENIED","message":"Denied"}}"""); fail("Expected error envelope") }
        catch (error: FriendsActionException) { assertEquals("PERMISSION_DENIED", error.code) }
    }

    @Test fun missingInsecureOrUntrustedEndpointIsRejectedWithoutInventingAUrl() {
        for (endpoint in listOf("", "http://demo.cloudfunctions.net/send", "https://untrusted.test/send", "https://user:password@demo.run.app/send")) {
            try { configuredFriendEndpoint(endpoint); fail("Expected configuration error") }
            catch (error: FriendsActionException) { assertEquals("FAILED_PRECONDITION", error.code) }
        }
        assertEquals("https", configuredFriendEndpoint("https://demo.cloudfunctions.net/sendFriendRequestByEmail").scheme)
    }
}
