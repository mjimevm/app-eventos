package plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model

data class AttendanceStatsDto(
    val totalAttendees: Int = 0,
    val attendingFriends: Int = 0,
    val isAttending: Boolean = false
)