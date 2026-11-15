package plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus

class AttendanceFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AttendanceFirebase {

    private val events
        get() = firestore.collection(FirebaseCollections.EVENTS)

    private val users
        get() = firestore.collection(FirebaseCollections.USERS)

    private fun currentUid(): String {
        return auth.currentUser?.uid?: throw IllegalStateException("Usuario no autenticado")
    }

    private fun attendees(eventId: String) =
        events.document(eventId).collection("attendees")

    private suspend fun requirePublishedEvent(eventId: String) {
        require(eventId.isNotBlank()) { "El ID del evento es obligatorio" }

        val event = events.document(eventId)
            .get()
            .await()
            .toObject(EventDto::class.java)
            ?: throw IllegalArgumentException("El evento no existe")

        require(event.status == EventStatus.PUBLISHED.name) { "El evento no está publicado" }
    }

    override suspend fun confirmAttendance(eventId: String) {
        requirePublishedEvent(eventId)
        val uid = currentUid()

        attendees(eventId).document(uid)
            .set(
                mapOf(
                    "confirmedAt" to System.currentTimeMillis()
                )
            ).await()
    }

    override suspend fun cancelAttendance(eventId: String) {
        val uid = currentUid()

        attendees(eventId)
            .document(uid)
            .delete()
            .await()
    }

    override suspend fun isAttending(eventId: String): Boolean {
        val uid = currentUid()

        return attendees(eventId)
            .document(uid)
            .get()
            .await()
            .exists()
    }

    override suspend fun getTotalAttendees(eventId: String): Int {
        val result = attendees(eventId)
            .count()
            .get(AggregateSource.SERVER)
            .await()

        return result.count.toInt()
    }

    override suspend fun getAttendingFriendsCount(
        eventId: String
    ): Int {
        val uid = currentUid()
        val friends = users.document(uid)
            .collection("friends")
            .get()
            .await()

        var count = 0

        for (friend in friends.documents) {
            val friendUid = friend.id
            val attending = attendees(eventId)
                .document(friendUid)
                .get()
                .await()
                .exists()
            if (attending) {
                count++
            }
        }
        return count
    }

    override suspend fun getAttendanceStats(
        eventId: String
    ): AttendanceStatsDto {
        val total = getTotalAttendees(eventId)
        val friends = getAttendingFriendsCount(eventId)
        val attending = isAttending(eventId)

        return AttendanceStatsDto(
            totalAttendees = total,
            attendingFriends = friends,
            isAttending = attending
        )
    }
}