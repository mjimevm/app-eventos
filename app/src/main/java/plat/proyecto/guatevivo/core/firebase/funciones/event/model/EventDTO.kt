package plat.proyecto.guatevivo.core.firebase.funciones.event.model

data class EventDto(
    val id: String = "",
    val nombreEvento: String = "",
    val ubicacion: String = "",
    val fecha: String = "",
    val hora: String = "",
    val descripcion: String = "",
    val incluirConfirmacion: Boolean = false,
    val organizadorId: String = "",
    val imagenUrl: String = "",
    val mapsUrl: String = "",
    val status: String = EventStatus.DRAFT.name,
    val visibility: String = EventVisibility.PRIVATE.name,
    val createdAt: Long = 0L
)
enum class EventStatus {
    DRAFT,
    PUBLISHED,
    CANCELLED
}
enum class EventVisibility {
    PUBLIC,
    PRIVATE
}