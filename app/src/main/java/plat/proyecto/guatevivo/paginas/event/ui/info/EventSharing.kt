package plat.proyecto.guatevivo.paginas.event.ui.info

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility

/** No event URL/deep-link system exists. Image and map URLs are not links to the event. */
fun EventDto.publicShareText(): String? {
    if (status != EventStatus.PUBLISHED.name || visibility != EventVisibility.PUBLIC.name) return null
    val dateFormat = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT)
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)
    val date: String
    val time: String
    try {
        date = LocalDate.parse(fecha.trim(), dateFormat).format(dateFormat)
        time = LocalTime.parse(hora.trim(), timeFormat).format(timeFormat)
    } catch (_: DateTimeParseException) { return null }
    return "$nombreEvento\nFecha: $date\nHora: $time\nUbicación: $ubicacion"
}
