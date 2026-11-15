package plat.proyecto.guatevivo.core.firebase.funciones.storage

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

class StorageFirebaseImpl(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : StorageFirebase {

    private fun currentUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
    }

    override suspend fun uploadEventImage(
        eventId: String,
        imageUri: Uri
    ): String {
        require(eventId.isNotBlank()) { "El ID del evento es obligatorio" }
        val uid = currentUid()

        val imageReference = storage.reference.child("events/$uid/$eventId/portada.jpg")
        val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()

        imageReference.putFile(
            imageUri,
            metadata
        ).await()

        return imageReference.downloadUrl.await().toString()
    }

    override suspend fun deleteEventImage(eventId: String) {
        require(eventId.isNotBlank()) { "El ID del evento es obligatorio" }
        val uid = currentUid()
        storage.reference.child("events/$uid/$eventId/portada.jpg").delete().await()
    }
}