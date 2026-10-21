package plat.proyecto.guatevivo.paginas.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.event.ui.create.navigateToCreateEvent
import plat.proyecto.guatevivo.paginas.event.ui.info.navigateToEventInfo
import plat.proyecto.guatevivo.paginas.home.ui.home.navigateToHome
import plat.proyecto.guatevivo.paginas.home.ui.search.navigateToSearch
import plat.proyecto.guatevivo.paginas.login.ui.onboarding.navigateToOnBoarding
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.BusinessAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.businessAccountScreen
import plat.proyecto.guatevivo.paginas.profile.ui.friends.friendsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.friends.navigateToFriends
import plat.proyecto.guatevivo.paginas.profile.ui.settings.navigateToSettings
import plat.proyecto.guatevivo.paginas.profile.ui.settings.settingsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.user.UserAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.user.userAccountScreen
import plat.proyecto.guatevivo.paginas.register.ui.choice.RegisterDestination


@Serializable
data object ProfileNestNav

fun NavController.navigateToProfileNest(navOptions: NavOptions? = null) {
    navigate(ProfileNestNav, navOptions)
}

fun NavGraphBuilder.profileNestNav(navController: NavController, isAccountBusiness: Boolean) {
    navigation<ProfileNestNav>(
        startDestination = if (isAccountBusiness) {
            BusinessAccountDestination
        } else {
            UserAccountDestination
        }
    ){
        businessAccountScreen(
            onHomeClick = {
                navController.navigateToHome()
            },
            onSearchClick = {
                navController.navigateToSearch()
            },
            onCreateClick = {
                navController.navigateToCreateEvent()
            },
            onProfileClick = {},//ya estamos aqui
            onAjustes = {
                navController.navigateToSettings()
            },
            onEvent = {
                navController.navigateToEventInfo()
            }
        )
        userAccountScreen(
            onHomeClick = {
                navController.navigateToHome()
            },
            onSearchClick = {
                navController.navigateToSearch()
            },
            onCreateClick = {
                navController.navigateToCreateEvent()
            },
            onProfileClick = {},//ya estamos aqui
            onAjustes = {
                navController.navigateToSettings()
            },
            onAmistades = {
                navController.navigateToFriends()
            },
            onEvent = {
                navController.navigateToEventInfo()
            }
        )
        friendsScreen(
            onBackClick = {navController.navigateUp()},
            onHomeClick = {
                navController.navigateToHome()
            },
            onSearchClick = {
                navController.navigateToSearch()
            },
            onCreateClick = {
                navController.navigateToCreateEvent()
            },
            onProfileClick = {
                navController.navigateToProfileNest()
            }
        )
        settingsScreen(
            onBackClick = {navController.navigateUp()},
            onLogoutClick = {
                navController.navigateToOnBoarding()
            },
            onHomeClick = {
                navController.navigateToHome()
            },
            onSearchClick = {
                navController.navigateToSearch()
            },
            onCreateClick = {
                navController.navigateToCreateEvent()
            },
            onProfileClick = {
                navController.navigateToProfileNest()
            }
        )
    }
}