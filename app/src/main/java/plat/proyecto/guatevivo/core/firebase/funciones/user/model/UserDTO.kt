package plat.proyecto.guatevivo.core.firebase.funciones.user.model

data class UserDto(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val accountType: String = AccountType.USER.name,
    val phone: String = "",
    val address: String = "",
    val description: String = ""
)
enum class AccountType {
    USER,
    BUSINESS
}