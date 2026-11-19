package plat.proyecto.guatevivo

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import plat.proyecto.guatevivo.core.EventosDb
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.FriendsFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.firebase.FriendsFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.asistencia.firebase.AttendanceFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.auth.AuthFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.event.firebase.EventFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.event.firebase.EventFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.storage.StorageFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.storage.StorageFirebaseImpl
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebaseImpl
import plat.proyecto.guatevivo.core.datastore.PreferencesRepository
import plat.proyecto.guatevivo.core.repository.AuthRepository
import plat.proyecto.guatevivo.core.repository.EventRepository
import plat.proyecto.guatevivo.core.repository.EventRepositoryImpl
import plat.proyecto.guatevivo.core.repository.UserRepository
import plat.proyecto.guatevivo.core.repository.FriendsRepository
import plat.proyecto.guatevivo.core.repository.AttendanceRepository

class GuateVivoApp : Application() {
    val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    val authFirebase: AuthFirebase by lazy {
        AuthFirebaseImpl(
            firebaseAuth
        )
    }
    val userFirebase: UserFirebase by lazy {
        UserFirebaseImpl(
            firestore,
            firebaseAuth
        )
    }
    val eventFirebase: EventFirebase by lazy {
        EventFirebaseImpl(
            firestore,
            firebaseAuth
        )
    }
    val storageFirebase: StorageFirebase by lazy {
        StorageFirebaseImpl(
            firebaseStorage,
            firebaseAuth
        )
    }
    val attendanceFirebase: AttendanceFirebase by lazy {
        AttendanceFirebaseImpl(
            firestore,
            firebaseAuth
        )
    }
    val friendsFirebase: FriendsFirebase by lazy {
        FriendsFirebaseImpl(
            firestore = firestore,
            auth = firebaseAuth
        )
    }

    val database: EventosDb by lazy {
        EventosDb.getInstance(applicationContext)
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(authFirebase, userFirebase)
    }

    val userRepository: UserRepository by lazy {
        UserRepository(userFirebase)
    }

    val friendsRepository: FriendsRepository by lazy {
        FriendsRepository(
            friendsFirebase,
            authRepository::getCurrentUserId
        ) {
            val uid = authRepository.getCurrentUserId() ?: return@FriendsRepository null
            userRepository.getUser(uid)?.accountType
        }
    }

    val attendanceRepository: AttendanceRepository by lazy {
        AttendanceRepository(attendanceFirebase)
    }

    val eventRepository: EventRepository by lazy {
        EventRepositoryImpl(eventFirebase, database.eventDao(), storageFirebase)
    }

    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(applicationContext)
    }

}
