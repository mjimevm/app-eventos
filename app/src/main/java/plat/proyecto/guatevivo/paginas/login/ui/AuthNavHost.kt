package plat.proyecto.guatevivo.paginas.login.ui

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import androidx.navigation.navigation
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.home.ui.home.navigateToHome
import plat.proyecto.guatevivo.paginas.login.ui.login.loginScreen
import plat.proyecto.guatevivo.paginas.login.ui.login.navigateToLogin
import plat.proyecto.guatevivo.paginas.login.ui.onboarding.onBoardingDestination
import plat.proyecto.guatevivo.paginas.login.ui.onboarding.onBoardingScreen
import plat.proyecto.guatevivo.paginas.register.navigateToRegisterNest
import plat.proyecto.guatevivo.paginas.register.registerNestNav

@Serializable
data object AuthNavHost

fun NavController.navigateToAuthNest(navOptions: NavOptions? = null) {
    navigate(AuthNavHost, navOptions)
}

fun NavGraphBuilder.AuthNestNav(navController: NavController) {
    navigation<AuthNavHost>(startDestination = onBoardingDestination){
        onBoardingScreen(
            onIniciarSesion = {
                navController.navigateToLogin()
            },
            onCrearCuenta = {
                navController.navigateToRegisterNest()
            }
        )
        loginScreen(
            onIniciarSesion = {
                navController.navigateToHome(
                    navOptions {
                        popUpTo<AuthNavHost> {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            },
            onCrearCuenta = {
                navController.navigateToRegisterNest()
            }
        )
        registerNestNav(navController)
    }
}