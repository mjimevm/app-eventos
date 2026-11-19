package plat.proyecto.guatevivo.core.firebase.funciones.user.model

import com.google.firebase.firestore.PropertyName

data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val accountType: String = AccountType.USER.name,
    val phone: String = "",
    val address: String = "",
    val description: String = "",
    val photoUrl: String = "",
    @get:PropertyName("isDiscoverable")
    @field:PropertyName("isDiscoverable")
    val isDiscoverable: Boolean = false,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    val isDeleted: Boolean = false,
    val nameNormalized: String = ""
)
enum class AccountType {
    USER,
    BUSINESS
}
