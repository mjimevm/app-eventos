package plat.proyecto.guatevivo.core.firebase.funciones.user.firebase

import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

interface UserFirebase {
    suspend fun createUser(user: UserDto)
    suspend fun getUser(uid: String): UserDto?
    suspend fun updateUser(user: UserDto)
    suspend fun deleteUser(uid: String)
    suspend fun getUserByEmail(email: String): UserDto?
}