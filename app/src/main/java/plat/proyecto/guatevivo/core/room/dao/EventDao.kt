package plat.proyecto.guatevivo.core.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import plat.proyecto.guatevivo.core.room.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEventsFlow(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events")
    suspend fun getAllEvents(): List<EventEntity>

    @Query("SELECT * FROM events WHERE id = :eventId")
    suspend fun getEventById(eventId: String): EventEntity?

    @Query("SELECT * FROM events WHERE status = 'PUBLISHED' AND visibility = 'PUBLIC'")
    suspend fun getPublicEvents(): List<EventEntity>

    @Query("DELETE FROM events WHERE status = 'PUBLISHED' AND visibility = 'PUBLIC'")
    suspend fun deletePublicEvents()

    @Transaction
    suspend fun replacePublicEvents(events: List<EventEntity>) {
        deletePublicEvents()
        insertEvents(events)
    }

    @Query("SELECT * FROM events WHERE organizadorId = :organizerId AND status = 'DRAFT'")
    suspend fun getDrafts(organizerId: String): List<EventEntity>

    @Query("SELECT * FROM events WHERE organizadorId = :organizerId")
    suspend fun getEventsByOrganizer(organizerId: String): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM events")
    suspend fun clearAll()
}
