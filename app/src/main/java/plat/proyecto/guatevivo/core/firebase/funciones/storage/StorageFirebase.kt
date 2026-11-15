package plat.proyecto.guatevivo.core.firebase.funciones.storage

import android.net.Uri

interface StorageFirebase {
    suspend fun uploadEventImage(
        eventId: String,
        imageUri: Uri
    ): String
    suspend fun deleteEventImage(
        eventId: String
    )
}