package plat.proyecto.guatevivo.core.firebase.funciones.asistencia

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility

class AttendanceFirebaseImplTest {
    @Test
    fun attendingEventQueryIncludesOnlyPublicPublishedEvents() {
        assertTrue(
            AttendanceFirebaseImpl.shouldIncludeAttendingEvent(
                EventStatus.PUBLISHED.name,
                EventVisibility.PUBLIC.name
            )
        )
        assertFalse(
            AttendanceFirebaseImpl.shouldIncludeAttendingEvent(
                EventStatus.DRAFT.name,
                EventVisibility.PUBLIC.name
            )
        )
        assertFalse(
            AttendanceFirebaseImpl.shouldIncludeAttendingEvent(
                EventStatus.PUBLISHED.name,
                EventVisibility.PRIVATE.name
            )
        )
    }
}
