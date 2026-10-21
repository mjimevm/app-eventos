package plat.proyecto.guatevivo.paginas.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.BusinessAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.businessAccountScreen
import plat.proyecto.guatevivo.paginas.profile.ui.friends.friendsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.settings.settingsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.user.UserAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.user.userAccountScreen
import plat.proyecto.guatevivo.paginas.register.ui.choice.RegisterDestination


@Serializable
data object ProfileNestNav

fun NavController.navigateToProfileNest(navOptions: NavOptions? = null) {
    navigate(ProfileNestNav, navOptions)
}

fun NavGraphBuilder.profileNestNav(navController: NavController, isBusiness: Boolean) {
    navigation<ProfileNestNav>(
        startDestination = if (isBusiness) {
            BusinessAccountDestination
        } else {
            UserAccountDestination
        }
    ){
        businessAccountScreen()
        userAccountScreen()
        friendsScreen()
        settingsScreen()
    }
}