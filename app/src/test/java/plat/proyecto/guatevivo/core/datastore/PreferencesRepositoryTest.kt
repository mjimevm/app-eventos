package plat.proyecto.guatevivo.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PreferencesRepositoryTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Test
    fun onboarding_defaultIsFalse() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        val repository = PreferencesRepository(testDataStore)

        val completed = repository.onboardingCompletedFlow.first()
        assertFalse(completed)
    }

    @Test
    fun onboarding_saveAndRead() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences_onboarding.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        val repository = PreferencesRepository(testDataStore)

        repository.setOnboardingCompleted(true)
        val completed = repository.onboardingCompletedFlow.first()
        assertTrue(completed)
    }

    @Test
    fun darkMode_defaultIsFalse() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences_dark.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        val repository = PreferencesRepository(testDataStore)

        val darkMode = repository.darkModeFlow.first()
        assertFalse(darkMode)
    }

    @Test
    fun darkMode_saveAndRead() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences_dark_save.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        val repository = PreferencesRepository(testDataStore)

        repository.setDarkMode(true)
        val darkMode = repository.darkModeFlow.first()
        assertTrue(darkMode)
    }
}
