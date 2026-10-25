package plat.proyecto.guatevivo.paginas.event.ui.create

sealed interface CreateEventEvent {
    data class UpdateNombreEvento(val nombre: String) : CreateEventEvent
    data class UpdateUbicacion(val ubicacion: String) : CreateEventEvent
    data class UpdateFecha(val fecha: String) : CreateEventEvent
    data class UpdateHora(val hora: String) : CreateEventEvent
    data class UpdateDescripcion(val descripcion: String) : CreateEventEvent
    data class UpdateIncluirConfirmacion(val incluir: Boolean) : CreateEventEvent
    data class UpdateImageUri(val uri: String?) : CreateEventEvent
    data object CreateEvent : CreateEventEvent
    data object ClearError : CreateEventEvent
}
