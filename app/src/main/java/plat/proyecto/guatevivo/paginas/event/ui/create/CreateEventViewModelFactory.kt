package plat.proyecto.guatevivo.paginas.event.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import plat.proyecto.guatevivo.core.repository.EventRepository

class CreateEventViewModelFactory(
    private val eventRepository: EventRepository,
    private val eventId: String? = null,
    private val currentUserId: () -> String? = { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid }
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        require(
            modelClass.isAssignableFrom(CreateEventViewModel::class.java)
        ) {
            "ViewModel desconocido: ${modelClass.name}"
        }

        return CreateEventViewModel(eventRepository, eventId, currentUserId) as T
    }
}
