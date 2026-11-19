package plat.proyecto.guatevivo.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guatevivo_preferences")

interface PreferencesRepository {
    val onboardingCompletedFlow: Flow<Boolean>
    // null means follow the system; existing true/false preferences remain manual overrides.
    val darkModeFlow: Flow<Boolean?>

    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setDarkMode(enabled: Boolean?)

    companion object {
        operator fun invoke(dataStore: DataStore<Preferences>): PreferencesRepository =
            PreferencesRepositoryImpl(dataStore)

        operator fun invoke(context: Context): PreferencesRepository =
            PreferencesRepositoryImpl(context.dataStore)
    }
}
