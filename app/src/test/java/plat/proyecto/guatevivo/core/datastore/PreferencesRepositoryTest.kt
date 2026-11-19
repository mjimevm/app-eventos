package plat.proyecto.guatevivo.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import plat.proyecto.guatevivo.testing.InMemoryPreferencesDataStore

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
            produceFile = { testFile },
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
            produceFile = { testFile },
        )
        val repository = PreferencesRepository(testDataStore)

        repository.setOnboardingCompleted(completed = true)
        val completed = repository.onboardingCompletedFlow.first()
        assertTrue(completed)
    }

    @Test
    fun darkMode_defaultsToSystem() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences_dark.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        val repository = PreferencesRepository(testDataStore)

        val darkMode = repository.darkModeFlow.first()
        assertNull(darkMode)
    }

    @Test
    fun darkMode_saveAndRead() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("test_preferences_dark_save.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        val repository = PreferencesRepository(testDataStore)

        repository.setDarkMode(enabled = true)
        val darkMode = repository.darkModeFlow.first()
        assertEquals(true, darkMode)
    }

    @Test
    fun darkMode_preservesExistingManualModesAndCanReturnToSystem() = testScope.runTest {
        val testDataStore = InMemoryPreferencesDataStore()
        val repository = PreferencesRepository(testDataStore)
        repository.setOnboardingCompleted(true)

        for (manualMode in listOf(false, true)) {
            repository.setDarkMode(manualMode)
            // Recreating the repository must retain the stored manual choice.
            assertEquals(manualMode, PreferencesRepository(testDataStore).darkModeFlow.first())
            repository.setDarkMode(null)
            assertNull(PreferencesRepository(testDataStore).darkModeFlow.first())
            assertTrue(repository.onboardingCompletedFlow.first())
        }
    }

    @Test
    fun darkMode_readsLegacyLightPreferenceFromDisk() = testScope.runTest {
        val testFile = tmpFolder.root.resolve("legacy_light.preferences_pb")
        val testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile },
        )
        testDataStore.edit { it[booleanPreferencesKey("dark_mode")] = false }

        assertEquals(false, PreferencesRepository(testDataStore).darkModeFlow.first())
    }
}
