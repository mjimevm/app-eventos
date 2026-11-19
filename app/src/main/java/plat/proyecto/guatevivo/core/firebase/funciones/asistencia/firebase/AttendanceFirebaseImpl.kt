package plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.model.AttendanceStatsDto
import plat.proyecto.guatevivo.core.firebase.FirebaseCollections
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventDto
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventStatus
import plat.proyecto.guatevivo.core.firebase.funciones.event.model.EventVisibility

class AttendanceFirebaseImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AttendanceFirebase {
    companion object {
        internal fun shouldIncludeAttendingEvent(status: String?, visibility: String?): Boolean =
            status == EventStatus.PUBLISHED.name && visibility == EventVisibility.PUBLIC.name
    }

    private val events
        get() = firestore.collection(FirebaseCollections.EVENTS)

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
        requirePublishedEvent(eventId)
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
        val friendUids = mutableSetOf<String>()
        val requests = firestore.collection(FirebaseCollections.FRIEND_REQUESTS)
        for (field in listOf("senderUid", "receiverUid")) {
            val records = requests
                .whereEqualTo(field, uid)
                .whereEqualTo("status", "ACCEPTED")
                .limit(50)
                .get()
                .await()
            for (record in records.documents) {
                val sender = record.getString("senderUid")
                val receiver = record.getString("receiverUid")
                val friendUid = if (sender == uid) receiver else sender
                if (!friendUid.isNullOrBlank() && friendUid != uid) friendUids += friendUid
            }
        }

        var count = 0
        for (friendUid in friendUids) {
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

    override suspend fun getAttendingEventIds(): List<String> {
        val uid = currentUid()
        val publishedEvents = events
            .whereEqualTo("status", EventStatus.PUBLISHED.name)
            .get()
            .await()
        val attendingIds = mutableListOf<String>()
        for (event in publishedEvents.documents) {
            if (!shouldIncludeAttendingEvent(event.getString("status"), event.getString("visibility"))) continue
            val attendee = event.reference.collection("attendees")
                .document(uid)
                .get()
                .await()
            if (attendee.exists()) attendingIds += event.id
        }
        return attendingIds.distinct()
    }
}