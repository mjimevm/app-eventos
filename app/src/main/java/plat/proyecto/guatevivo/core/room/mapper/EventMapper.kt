package plat.proyecto.guatevivo.core.room.mapper

import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.room.entity.EventEntity

fun EventDto.toEntity(): EventEntity {
    return EventEntity(
        id = id,
        nombreEvento = nombreEvento,
        ubicacion = ubicacion,
        fecha = fecha,
        hora = hora,
        descripcion = descripcion,
        incluirConfirmacion = incluirConfirmacion,
        organizadorId = organizadorId,
        imagenUrl = imagenUrl,
        mapsUrl = mapsUrl,
        status = status,
        visibility = visibility,
        createdAt = createdAt,
    )
}

fun EventEntity.toDto(): EventDto {
    return EventDto(
        id = id,
        nombreEvento = nombreEvento,
        ubicacion = ubicacion,
        fecha = fecha,
        hora = hora,
        descripcion = descripcion,
        incluirConfirmacion = incluirConfirmacion,
        organizadorId = organizadorId,
        imagenUrl = imagenUrl,
        mapsUrl = mapsUrl,
        status = status,
        visibility = visibility,
        createdAt = createdAt,
    )
}

fun List<EventDto>.toEntities(): List<EventEntity> = map { it.toEntity() }
fun List<EventEntity>.toDtos(): List<EventDto> = map { it.toDto() }
