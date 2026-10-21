package plat.proyecto.guatevivo.paginas.register

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.home.ui.home.navigateToHome
import plat.proyecto.guatevivo.paginas.register.ui.business.navigateToRegisterBusiness
import plat.proyecto.guatevivo.paginas.register.ui.business.registerBusinessScreen
import plat.proyecto.guatevivo.paginas.register.ui.choice.RegisterDestination
import plat.proyecto.guatevivo.paginas.register.ui.choice.registerScreen
import plat.proyecto.guatevivo.paginas.register.ui.user.navigateToRegisterUser
import plat.proyecto.guatevivo.paginas.register.ui.user.registerUserScreen

@Serializable
data object RegisterNestNav

fun NavController.navigateToRegisterNest(navOptions: NavOptions? = null) {
    navigate(RegisterNestNav, navOptions)
}

fun NavGraphBuilder.registerNestNav(navController: NavController) {
    navigation<RegisterNestNav>(startDestination = RegisterDestination){
        registerScreen(
            onCreateNormalAccount = {
                navController.navigateToRegisterUser()
            },
            onCreateBussinesAccount = {
                navController.navigateToRegisterBusiness()
            },
            onBack = {navController.navigateUp()}
        )
        registerBusinessScreen(
            onCreate = {
                navController.navigateToHome()
            }
        )
        registerUserScreen(
            onCreate = {
                navController.navigateToHome()
            }
        )
    }
}