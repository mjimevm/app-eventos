package plat.proyecto.guatevivo.paginas.login.ui.login

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
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.UserRepository

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthFirebase(val shouldFail: Boolean = false) : AuthFirebase {
        override suspend fun register(email: String, password: String): String = "uid_1"
        override suspend fun login(email: String, password: String): String {
            if (shouldFail) throw RuntimeException("Invalid credentials")
            return "uid_1"
        }
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String? = "uid_1"
        override fun getCurrentUserEmail(): String? = "test@example.com"
        override fun isLoggedIn(): Boolean = true
        override fun logout() {}
    }

    private class FakeUserFirebase(val accountType: String = AccountType.USER.name) : UserFirebase {
        override suspend fun createUser(user: UserDto) {}
        override suspend fun getUser(uid: String): UserDto = UserDto(uid = uid, accountType = accountType)
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    @Test
    fun login_emptyFields_showsError() = runTest {
        val viewModel = LoginViewModel(
            AuthRepository(FakeAuthFirebase(), FakeUserFirebase()),
            UserRepository(FakeUserFirebase())
        )
        viewModel.onEvent(LoginEvent.OnLoginClicked)
        val state = viewModel.state.value
        assertEquals("Por favor ingresa tu correo y contraseña", state.errorMessage)
        assertFalse(state.isLoginSuccessful)
    }

    @Test
    fun login_success_userTypeRetrieved() = runTest {
        val viewModel = LoginViewModel(
            AuthRepository(FakeAuthFirebase(), FakeUserFirebase(AccountType.BUSINESS.name)),
            UserRepository(FakeUserFirebase(AccountType.BUSINESS.name))
        )
        viewModel.onEvent(LoginEvent.OnEmailChanged("business@example.com"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("password123"))
        viewModel.onEvent(LoginEvent.OnLoginClicked)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isLoginSuccessful)
        assertEquals(AccountType.BUSINESS.name, state.accountType)
        assertNull(state.errorMessage)
    }

    @Test
    fun login_failure_showsError() = runTest {
        val viewModel = LoginViewModel(
            AuthRepository(FakeAuthFirebase(shouldFail = true), FakeUserFirebase()),
            UserRepository(FakeUserFirebase())
        )
        viewModel.onEvent(LoginEvent.OnEmailChanged("test@example.com"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("wrongpassword"))
        viewModel.onEvent(LoginEvent.OnLoginClicked)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoginSuccessful)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun preventDuplicateRequests_whileLoading() = runTest {
        val viewModel = LoginViewModel(
            AuthRepository(FakeAuthFirebase(), FakeUserFirebase()),
            UserRepository(FakeUserFirebase())
        )
        viewModel.onEvent(LoginEvent.OnEmailChanged("test@example.com"))
        viewModel.onEvent(LoginEvent.OnPasswordChanged("password123"))

        viewModel.onEvent(LoginEvent.OnLoginClicked)
        assertTrue(viewModel.state.value.isLoading)

        viewModel.onEvent(LoginEvent.OnLoginClicked)
    }
}
