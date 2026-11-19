package plat.proyecto.guatevivo.core

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import plat.proyecto.guatevivo.core.room.dao.EventDao
import plat.proyecto.guatevivo.core.room.entity.EventEntity

@Database(
    entities = [EventEntity::class],
    version = 1,
    exportSchema = false
)
abstract class EventosDb : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {

        @Volatile
        private var INSTANCE: EventosDb? = null

        fun getInstance(context: Context): EventosDb {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    EventosDb::class.java,
                    "guatevivo_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}