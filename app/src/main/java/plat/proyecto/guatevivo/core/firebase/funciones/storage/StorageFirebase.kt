package plat.proyecto.guatevivo.core.firebase.funciones.storage

import android.net.Uri

interface StorageFirebase {
    suspend fun uploadEventImage(
        eventId: String,
        imageUri: Uri
    ): String
    suspend fun uploadEventImageVersion(eventId: String, imageUri: Uri): String =
        throw UnsupportedOperationException("No se puede actualizar la imagen del evento")
    suspend fun deleteEventImage(
        eventId: String
    )
}
