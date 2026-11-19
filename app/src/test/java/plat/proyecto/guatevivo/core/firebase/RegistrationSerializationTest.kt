package plat.proyecto.guatevivo.core.firebase

import com.google.firebase.firestore.util.CustomClassMapper
import org.junit.Assert.*
import org.junit.Test
import plat.proyecto.guatevivo.core.firebase.funciones.amigos.model.publicProfile
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.UserDto

class RegistrationSerializationTest {
    @Test
    fun businessRegistrationFieldNamesMatchSecurityRules() {
        val user = UserDto(uid = "business", name = "Empresa", accountType = AccountType.BUSINESS.name)
        for (document in listOf(user, user.publicProfile())) {
            val fields = CustomClassMapper.convertToPlainJavaTypes(document) as Map<*, *>
            assertEquals(fields.toString(), false, fields["isDeleted"])
            assertEquals(fields.toString(), false, fields["isDiscoverable"])
            assertEquals("BUSINESS", fields["accountType"])
            assertFalse(fields.containsKey("deleted"))
            assertFalse(fields.containsKey("discoverable"))
        }
    }

    @Test
    fun publicProjectionHasOnlyTheFieldsAllowedAtRegistration() {
        val user = UserDto(uid = "business", name = "Empresa", email = "private@example.com",
            phone = "12345678", address = "Privada", description = "Privada", accountType = "BUSINESS")
        val fields = CustomClassMapper.convertToPlainJavaTypes(user.publicProfile()) as Map<*, *>
        assertEquals(setOf("uid", "name", "nameNormalized", "photoUrl", "accountType", "isDiscoverable", "isDeleted"), fields.keys)
    }

    @Test
    fun booleanFieldsRoundTripWithTheirCanonicalNames() {
        val user = UserDto(uid = "business", name = "Empresa", accountType = "BUSINESS",
            isDeleted = true, isDiscoverable = true)
        val userFields = CustomClassMapper.convertToPlainJavaTypes(user)
        assertEquals(user, CustomClassMapper.convertToCustomClass(userFields, UserDto::class.java, null))
        val publicProfile = user.publicProfile()
        val publicFields = CustomClassMapper.convertToPlainJavaTypes(publicProfile)
        assertEquals(publicProfile, CustomClassMapper.convertToCustomClass(publicFields, publicProfile.javaClass, null))
    }
}
