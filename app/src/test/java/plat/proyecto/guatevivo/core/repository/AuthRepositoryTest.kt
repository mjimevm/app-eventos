package plat.proyecto.guatevivo.core.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class AuthRepositoryTest {

    private class FakeAuthFirebase(
        var shouldFailRegister: Boolean = false,
        var registeredUid: String = "test_uid_123"
    ) : AuthFirebase {
        var registerCalled = 0
        override suspend fun register(email: String, password: String): String {
            registerCalled++
            if (shouldFailRegister) throw RuntimeException("Auth failed")
            return registeredUid
        }
        override suspend fun login(email: String, password: String): String = registeredUid
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String? = registeredUid
        override fun getCurrentUserEmail(): String? = "test@example.com"
        override fun isLoggedIn(): Boolean = true
        override fun logout() {}
    }

    private class FakeUserFirebase(
        var shouldFailCreate: Boolean = false
    ) : UserFirebase {
        var createCalled = 0
        var lastCreatedUser: UserDto? = null
        override suspend fun createUser(user: UserDto) {
            createCalled++
            if (shouldFailCreate) throw RuntimeException("Firestore failed")
            lastCreatedUser = user
        }
        override suspend fun getUser(uid: String): UserDto? = null
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    @Test
    fun register_success() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase()
        val repo = AuthRepository(auth, user)

        val uid = repo.register("test@example.com", "password123", UserDto(name = "Test"))
        assertEquals("test_uid_123", uid)
        assertEquals(1, auth.registerCalled)
        assertEquals(1, user.createCalled)
        assertNotNull(user.lastCreatedUser)
        assertEquals("test_uid_123", user.lastCreatedUser?.uid)
    }

    @Test
    fun register_authFailure_throwsException() = runTest {
        val auth = FakeAuthFirebase(shouldFailRegister = true)
        val user = FakeUserFirebase()
        val repo = AuthRepository(auth, user)

        try {
            repo.register("test@example.com", "password123", UserDto(name = "Test"))
            fail("Expected exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Error al registrar la cuenta en Authentication") == true)
        }
        assertEquals(1, auth.registerCalled)
        assertEquals(0, user.createCalled)
    }

    @Test
    fun register_firestoreFailure_partialFailureHandled() = runTest {
        val auth = FakeAuthFirebase(registeredUid = "partial_uid_456")
        val user = FakeUserFirebase(shouldFailCreate = true)
        val repo = AuthRepository(auth, user)

        try {
            repo.register("test@example.com", "password123", UserDto(name = "Test"))
            fail("Expected exception")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("falló al guardar el perfil en Firestore") == true)
            assertTrue(e.message?.contains("partial_uid_456") == true)
        }
        assertEquals(1, auth.registerCalled)
        assertEquals(1, user.createCalled)
        // Verify auth account was NOT automatically deleted
        assertEquals("partial_uid_456", auth.getCurrentUserId())
    }
}
