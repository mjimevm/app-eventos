package plat.proyecto.guatevivo.paginas.home.ui

import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility
import plat.proyecto.guatevivo.paginas.home.ui.home.ProximamenteItem
import plat.proyecto.guatevivo.core.repository.EventReadException
import plat.proyecto.guatevivo.core.repository.EventReadFailure

private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
private val timeFormat = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)
private val spanish = Locale.forLanguageTag("es-GT")

private fun EventDto.startTime(): LocalDateTime? = try {
    LocalDate.parse(fecha.trim(), dateFormat).atTime(LocalTime.parse(hora.trim(), timeFormat))
} catch (_: DateTimeParseException) { null }

// Both screens expose upcoming events. Missing/invalid dates cannot be ordered reliably.
fun upcomingPublicEvents(events: List<EventDto>, now: LocalDateTime): List<ProximamenteItem> = events
    .filter { it.id.isNotBlank() && it.status == EventStatus.PUBLISHED.name && it.visibility == EventVisibility.PUBLIC.name }
    .distinctBy { it.id }
    .mapNotNull { event -> event.startTime()?.let { event to it } }
    .filter { (_, start) -> !start.isBefore(now.truncatedTo(ChronoUnit.MINUTES)) }
    .sortedWith(compareBy<Pair<EventDto, LocalDateTime>> { it.second }.thenBy { it.first.id })
    .map { (event, start) ->
        ProximamenteItem(
            title = event.nombreEvento,
            fecha = start.format(DateTimeFormatter.ofPattern("MMM", spanish)).removeSuffix(".").uppercase(spanish),
            dia = start.dayOfMonth,
            ubicacion = event.ubicacion,
            imageResId = 0,
            imageUrl = event.imagenUrl,
            eventId = event.id,
            fullDate = start.format(dateFormat),
            hora = start.format(timeFormat),
            descripcion = event.descripcion
        )
    }

fun String.normalizedEventQuery(): String = Normalizer.normalize(this, Normalizer.Form.NFD)
    .replace(Regex("\\p{M}+"), "")
    .lowercase(Locale.ROOT).trim().replace(Regex("\\s+"), " ")

fun List<ProximamenteItem>.searchEvents(query: String): List<ProximamenteItem> {
    val terms = query.normalizedEventQuery().split(' ').filter { it.isNotEmpty() }
    return if (terms.isEmpty()) this else filter { event ->
        val text = listOf(event.title, event.ubicacion, event.descripcion).joinToString(" ").normalizedEventQuery()
        terms.all { it in text }
    }
}

fun catalogErrorMessage(error: Exception): String = when {
    error is EventReadException && error.reason == EventReadFailure.PERMISSION_DENIED ->
        "No tienes permiso para consultar los eventos. Inténtalo de nuevo o revisa tu sesión."
    error is EventReadException && error.reason == EventReadFailure.UNAUTHENTICATED ->
        "Inicia sesión para consultar los eventos."
    else -> "No se pudieron cargar los eventos. Inténtalo de nuevo."
}
