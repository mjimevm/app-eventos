package plat.proyecto.guatevivo.paginas.profile.ui.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import plat.proyecto.guatevivo.core.datastore.PreferencesRepository
import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto
import plat.proyecto.guatevivo.core.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeAuthFirebase : AuthFirebase {
        var logoutCalled = false
        override suspend fun register(email: String, password: String): String = ""
        override suspend fun login(email: String, password: String): String = ""
        override suspend fun resetPassword(email: String) {}
        override fun getCurrentUserId(): String? = null
        override fun getCurrentUserEmail(): String? = null
        override fun isLoggedIn(): Boolean = true
        override fun logout() { logoutCalled = true }
    }

    private class FakeUserFirebase : UserFirebase {
        override suspend fun createUser(user: UserDto) {}
        override suspend fun getUser(uid: String): UserDto? = null
        override suspend fun updateUser(user: UserDto) {}
        override suspend fun deleteUser(uid: String) {}
        override suspend fun getUserByEmail(email: String): UserDto? = null
    }

    @Test
    fun darkModeToggle_updatesState() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("settings_test.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = testScope, produceFile = { testFile })
        val prefRepo = PreferencesRepository(dataStore)
        val authRepo = AuthRepository(FakeAuthFirebase(), FakeUserFirebase())

        val viewModel = SettingsViewModel(authRepo, prefRepo)

        assertFalse(viewModel.state.value.darkMode)

        viewModel.onEvent(SettingsEvent.OnDarkModeToggled(true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value.darkMode)
    }

    @Test
    fun logout_callsAuthRepository() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("settings_test_logout.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = testScope, produceFile = { testFile })
        val prefRepo = PreferencesRepository(dataStore)
        val fakeAuth = FakeAuthFirebase()
        val authRepo = AuthRepository(fakeAuth, FakeUserFirebase())

        val viewModel = SettingsViewModel(authRepo, prefRepo)
        viewModel.logout()

        assertTrue(fakeAuth.logoutCalled)
    }
}
