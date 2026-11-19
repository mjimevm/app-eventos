package plat.proyecto.guatevivo.core.repository

import java.io.IOException
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto

data class PublicEventsResult(
    val events: List<EventDto>,
    val isFromCache: Boolean = false
)

enum class EventReadFailure { CONNECTION, PERMISSION_DENIED, UNAUTHENTICATED }

class EventReadException(val reason: EventReadFailure, cause: Throwable) : Exception(cause.message, cause)

internal fun Throwable.isEventConnectionFailure(): Boolean =
    this is IOException || this is EventReadException && reason == EventReadFailure.CONNECTION
