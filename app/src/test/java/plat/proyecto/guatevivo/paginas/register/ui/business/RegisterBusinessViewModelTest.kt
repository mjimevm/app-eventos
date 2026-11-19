package plat.proyecto.guatevivo.paginas.register.ui.business

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
class RegisterBusinessViewModelTest {

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
        var registerCalled = 0
        override suspend fun register(email: String, password: String): String {
            registerCalled++
            return "biz_uid_1"
        }
        override suspend fun login(email: String, password: String): String = ""
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String? = null
        override fun getCurrentUserEmail(): String? = null
        override fun isLoggedIn(): Boolean = false
        override fun logout() {}
    }

    private class FakeUserFirebase : UserFirebase {
        var createCalled = 0
        override suspend fun createUser(user: UserDto) {
            createCalled++
        }
        override suspend fun getUser(uid: String): UserDto? = null
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    @Test
    fun registerBusiness_validationFailures() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase()
        val viewModel = RegisterBusinessViewModel(AuthRepository(auth, user))

        viewModel.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        assertNotNull(viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isSuccess)

        viewModel.onEvent(RegisterBusinessEvent.OnEmpresaChanged("GuateCorp"))
        viewModel.onEvent(RegisterBusinessEvent.OnCorreoChanged("corp@example.com"))
        viewModel.onEvent(RegisterBusinessEvent.OnPasswordChanged("password123"))
        viewModel.onEvent(RegisterBusinessEvent.OnConfirmPasswordChanged("password123"))
        viewModel.onEvent(RegisterBusinessEvent.OnAceptoTerminosChanged(false))
        viewModel.onEvent(RegisterBusinessEvent.OnRepresentanteChanged(false))
        viewModel.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        assertNotNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun registerBusiness_success() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase()
        val viewModel = RegisterBusinessViewModel(AuthRepository(auth, user))

        viewModel.onEvent(RegisterBusinessEvent.OnEmpresaChanged("GuateCorp"))
        viewModel.onEvent(RegisterBusinessEvent.OnCorreoChanged("corp@example.com"))
        viewModel.onEvent(RegisterBusinessEvent.OnTelefonoChanged("22223333"))
        viewModel.onEvent(RegisterBusinessEvent.OnDireccionChanged("Zona 10"))
        viewModel.onEvent(RegisterBusinessEvent.OnDescripcionChanged("Eventos corporativos"))
        viewModel.onEvent(RegisterBusinessEvent.OnPasswordChanged("password123"))
        viewModel.onEvent(RegisterBusinessEvent.OnConfirmPasswordChanged("password123"))
        viewModel.onEvent(RegisterBusinessEvent.OnAceptoTerminosChanged(true))
        viewModel.onEvent(RegisterBusinessEvent.OnRepresentanteChanged(true))
        viewModel.onEvent(RegisterBusinessEvent.OnRegisterClicked)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.isSuccess)
        assertNull(state.errorMessage)
        assertEquals(1, auth.registerCalled)
        assertEquals(1, user.createCalled)
    }
}
