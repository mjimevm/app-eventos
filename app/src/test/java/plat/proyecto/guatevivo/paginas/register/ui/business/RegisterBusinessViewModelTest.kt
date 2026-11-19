package plat.proyecto.guatevivo.paginas.register.ui.business

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.CompletableDeferred
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
        var failure: Exception? = null
        var currentUid: String? = null
        var currentEmail: String? = null
        var lastPassword: String? = null
        override suspend fun register(email: String, password: String): String {
            registerCalled++
            failure?.let { throw it }
            currentUid = "biz_uid_1"
            currentEmail = email
            lastPassword = password
            return "biz_uid_1"
        }
        override suspend fun login(email: String, password: String): String = ""
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String? = currentUid
        override fun getCurrentUserEmail(): String? = currentEmail
        override fun isLoggedIn(): Boolean = false
        override fun logout() {}
    }

    private class FakeUserFirebase : UserFirebase {
        var createCalled = 0
        var failure: Exception? = null
        var saved: UserDto? = null
        var gate: CompletableDeferred<Unit>? = null
        override suspend fun createUser(user: UserDto) {
            createCalled++
            gate?.await()
            failure?.let { throw it }
            saved = user
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
        assertEquals("BUSINESS", user.saved?.accountType)
        assertEquals("biz_uid_1", user.saved?.uid)
    }

    private fun validForm(vm: RegisterBusinessViewModel) {
        vm.onEvent(RegisterBusinessEvent.OnEmpresaChanged("  GuateCorp  "))
        vm.onEvent(RegisterBusinessEvent.OnCorreoChanged("  CORP@example.com  "))
        vm.onEvent(RegisterBusinessEvent.OnPasswordChanged(" password123 "))
        vm.onEvent(RegisterBusinessEvent.OnConfirmPasswordChanged(" password123 "))
        vm.onEvent(RegisterBusinessEvent.OnAceptoTerminosChanged(true))
        vm.onEvent(RegisterBusinessEvent.OnRepresentanteChanged(true))
    }

    @Test
    fun firestoreDenied_isVisible_andRetryDoesNotCreateAuthAgain() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase()
        // The SDK Code enum requires Android SparseArray; keep this JVM fake at the gateway boundary.
        user.failure = IllegalStateException("PERMISSION_DENIED: Missing or insufficient permissions")
        val vm = RegisterBusinessViewModel(AuthRepository(auth, user))
        validForm(vm)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSuccess)
        assertFalse(vm.state.value.isLoading)
        assertEquals("biz_uid_1", vm.state.value.partialUid)
        assertTrue(vm.state.value.errorMessage!!.contains("PERMISSION_DENIED"))
        assertTrue(vm.state.value.errorMessage!!.contains("publicProfiles/biz_uid_1"))

        user.failure = null
        vm.onEvent(RegisterBusinessEvent.OnCorreoChanged("other@example.com"))
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isSuccess)
        assertEquals(1, auth.registerCalled)
        assertEquals(2, user.createCalled)
        assertEquals("corp@example.com", user.saved?.email)
        assertEquals("GuateCorp", user.saved?.name)
        assertEquals("BUSINESS", user.saved?.accountType)
        assertEquals(" password123 ", auth.lastPassword)
    }

    @Test
    fun authenticationFailure_preservesDetailsAndDoesNotWriteProfile() = runTest {
        val auth = FakeAuthFirebase().apply { failure = IllegalStateException("ERROR_EMAIL_ALREADY_IN_USE") }
        val user = FakeUserFirebase()
        val vm = RegisterBusinessViewModel(AuthRepository(auth, user))
        validForm(vm)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSuccess)
        assertFalse(vm.state.value.isLoading)
        assertNull(vm.state.value.partialUid)
        assertTrue(vm.state.value.errorMessage!!.contains("ERROR_EMAIL_ALREADY_IN_USE"))
        assertEquals(0, user.createCalled)
    }

    @Test
    fun registrationWaitsForProfile_andIgnoresDoubleClick() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase().apply { gate = CompletableDeferred() }
        val vm = RegisterBusinessViewModel(AuthRepository(auth, user))
        validForm(vm)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.runCurrent()
        assertTrue(vm.state.value.isLoading)
        assertFalse(vm.state.value.isSuccess)
        assertEquals(1, auth.registerCalled)
        user.gate!!.complete(Unit)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.state.value.isSuccess)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, auth.registerCalled)
    }

    @Test
    fun invalidFieldsNeverCallFirebase() = runTest {
        val changes = listOf(
            RegisterBusinessEvent.OnCorreoChanged("mal correo@ejemplo.com"),
            RegisterBusinessEvent.OnCorreoChanged("sin-arroba"),
            RegisterBusinessEvent.OnEmpresaChanged("x".repeat(121)),
            RegisterBusinessEvent.OnPasswordChanged("12345"),
            RegisterBusinessEvent.OnConfirmPasswordChanged("diferente"),
            RegisterBusinessEvent.OnAceptoTerminosChanged(false),
            RegisterBusinessEvent.OnRepresentanteChanged(false)
        )
        for (change in changes) {
            val auth = FakeAuthFirebase()
            val user = FakeUserFirebase()
            val vm = RegisterBusinessViewModel(AuthRepository(auth, user))
            validForm(vm)
            vm.onEvent(change)
            vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
            testDispatcher.scheduler.advanceUntilIdle()
            assertNotNull(vm.state.value.errorMessage)
            assertFalse(vm.state.value.isSuccess)
            assertEquals(0, auth.registerCalled)
            assertEquals(0, user.createCalled)
        }
    }

    @Test
    fun retryAfterSessionChangeDoesNotWriteToAnotherAccount() = runTest {
        val auth = FakeAuthFirebase()
        val user = FakeUserFirebase().apply { failure = IllegalStateException("PERMISSION_DENIED") }
        val vm = RegisterBusinessViewModel(AuthRepository(auth, user))
        validForm(vm)
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        auth.currentUid = "another_uid"
        user.failure = null
        vm.onEvent(RegisterBusinessEvent.OnRegisterClicked)
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(vm.state.value.isSuccess)
        assertTrue(vm.state.value.errorMessage!!.contains("sesión cambió"))
        assertEquals(1, user.createCalled)
    }
}
