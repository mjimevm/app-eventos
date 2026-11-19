package plat.proyecto.guatevivo.core.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey
    val id: String,
    val nombreEvento: String,
    val ubicacion: String,
    val fecha: String,
    val hora: String,
    val descripcion: String,
    val incluirConfirmacion: Boolean,
    val organizadorId: String,
    val imagenUrl: String,
    val mapsUrl: String,
    val status: String,
    val visibility: String,
    val createdAt: Long,
)
