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
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.AuthRepository
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

    @Test
    fun loadBusinessProfile_success() = runTest {
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())
        val userRepo = UserRepository(FakeUserFirebase())
        val viewModel = BusinessViewModel(authRepo, userRepo)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals("Guate Business", state.empresaNombre)
        assertFalse(state.isLoading)
    }
}
