package plat.proyecto.guatevivo.paginas.register

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.register.ui.business.registerBusinessScreen
import plat.proyecto.guatevivo.paginas.register.ui.choice.RegisterDestination
import plat.proyecto.guatevivo.paginas.register.ui.choice.registerScreen
import plat.proyecto.guatevivo.paginas.register.ui.user.registerUserScreen

@Serializable
data object RegisterNestNav

fun NavController.navigateToRegisterNest(navOptions: NavOptions? = null) {
    navigate(RegisterNestNav, navOptions)
}

fun NavGraphBuilder.registerNestNav(navController: NavController) {
    navigation<RegisterNestNav>(startDestination = RegisterDestination){
        registerScreen()
        registerBusinessScreen()
        registerUserScreen()
    }
}