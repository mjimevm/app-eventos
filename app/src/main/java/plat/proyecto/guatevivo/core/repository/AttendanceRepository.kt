package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto

class AttendanceRepository(private val firebase: AttendanceFirebase) {
    suspend fun getStats(eventId: String): AttendanceStatsDto =
        firebase.getAttendanceStats(eventId)

    suspend fun confirm(eventId: String) {
        firebase.confirmAttendance(eventId)
    }

    suspend fun cancel(eventId: String) {
        firebase.cancelAttendance(eventId)
    }

    suspend fun getAttendingEventIds(): List<String> =
        firebase.getAttendingEventIds()
}
