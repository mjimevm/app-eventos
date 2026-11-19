package plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase

import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto

interface AttendanceFirebase {
    suspend fun confirmAttendance(eventId: String)
    suspend fun cancelAttendance(eventId: String)
    suspend fun isAttending(eventId: String): Boolean
    suspend fun getTotalAttendees(eventId: String): Int
    suspend fun getAttendingFriendsCount(eventId: String): Int
    suspend fun getAttendanceStats(eventId: String): AttendanceStatsDto
    suspend fun getAttendingEventIds(): List<String>
}