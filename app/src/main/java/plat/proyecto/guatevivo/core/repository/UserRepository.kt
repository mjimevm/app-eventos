package plat.proyecto.guatevivo.core.repository

import plat.proyecto.guatevivo.core.firebase.funciones.user.firebase.UserFirebase
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class UserRepository(
    private val userFirebase: UserFirebase
) {
    suspend fun createUser(user: UserDto) {
        userFirebase.createUser(user)
    }

    suspend fun getUser(uid: String): UserDto? {
        return userFirebase.getUser(uid)
    }

    suspend fun updateUser(user: UserDto) {
        userFirebase.updateUser(user)
    }

    suspend fun deleteUser(uid: String) {
        userFirebase.deleteUser(uid)
    }

    suspend fun getUserByEmail(email: String): UserDto? {
        return userFirebase.getUserByEmail(email)
    }
}
