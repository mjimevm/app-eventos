package plat.proyecto.guatevivo.paginas.profile

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.navOptions
import androidx.navigation.navigation
import androidx.navigation.compose.composable
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.CancellationException
import plat.proyecto.guatevivo.GuateVivoApp
import plat.proyecto.guatevivo.core.firebase.funciones.user.model.AccountType
import kotlinx.serialization.Serializable
import plat.proyecto.guatevivo.paginas.event.ui.create.navigateToCreateEvent
import plat.proyecto.guatevivo.paginas.event.ui.info.navigateToEventInfo
import plat.proyecto.guatevivo.paginas.home.ui.home.navigateToHome
import plat.proyecto.guatevivo.paginas.home.ui.search.navigateToSearch
import plat.proyecto.guatevivo.paginas.login.ui.navigateToAuthNest
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.BusinessAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.bussines.businessAccountScreen
import plat.proyecto.guatevivo.paginas.profile.ui.friends.friendsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.friends.navigateToFriends
import plat.proyecto.guatevivo.paginas.profile.ui.friends.navigateToPublicProfile
import plat.proyecto.guatevivo.paginas.profile.ui.settings.navigateToSettings
import plat.proyecto.guatevivo.paginas.profile.ui.settings.settingsScreen
import plat.proyecto.guatevivo.paginas.profile.ui.user.UserAccountDestination
import plat.proyecto.guatevivo.paginas.profile.ui.user.userAccountScreen


@Serializable
data object ProfileNestNav

@Serializable
private data object ProfileStartDestination

fun NavController.navigateToProfileNest(navOptions: NavOptions? = null) {
    navigate(ProfileNestNav, navOptions)
}

fun NavGraphBuilder.profileNestNav(
    navController: NavController,
    bottomBarOptions: NavOptions
    ) {
    navigation<ProfileNestNav>(
        startDestination = ProfileStartDestination
    ){
        composable<ProfileStartDestination> {
            val app = LocalContext.current.applicationContext as GuateVivoApp
            var error by remember { mutableStateOf<String?>(null) }
            var attempt by remember { mutableIntStateOf(0) }
            LaunchedEffect(attempt) {
                error = null
                try {
                    val uid = checkNotNull(app.authRepository.getCurrentUserId()) { "No hay sesión autenticada" }
                    val profile = checkNotNull(app.userRepository.getUser(uid)) {
                        "La cuenta no tiene perfil en Firestore. Completa el registro pendiente."
                    }
                    check(profile.accountType in AccountType.entries.map { it.name }) { "Tipo de cuenta inválido" }
                    val options = navOptions {
                        popUpTo<ProfileStartDestination> { inclusive = true }
                        launchSingleTop = true
                    }
                    if (profile.accountType == AccountType.BUSINESS.name) {
                        navController.navigate(BusinessAccountDestination, options)
                    } else {
                        navController.navigate(UserAccountDestination, options)
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    error = e.localizedMessage ?: "No se pudo leer el tipo de cuenta"
                }
            }
            Column {
                if (error == null) CircularProgressIndicator()
                else {
                    Text(checkNotNull(error))
                    TextButton(onClick = { attempt++ }) { Text("Reintentar cargar perfil") }
                }
            }
        }
        businessAccountScreen(
            onHomeClick = {
                navController.navigateToHome(bottomBarOptions)
            },
            onSearchClick = {
                navController.navigateToSearch(bottomBarOptions)
            },
            onCreateClick = {
                navController.navigateToCreateEvent(bottomBarOptions)
            },
            onProfileClick = {},//ya estamos aqui
            onAjustes = {
                navController.navigateToSettings()
            },
            onEvent = { eventId ->
                navController.navigateToEventInfo(eventId)
            }
        )
        userAccountScreen(
            onHomeClick = {
                navController.navigateToHome(bottomBarOptions)
            },
            onSearchClick = {
                navController.navigateToSearch(bottomBarOptions)
            },
            onCreateClick = {
                navController.navigateToCreateEvent(bottomBarOptions)
            },
            onProfileClick = {},//ya estamos aqui
            onAjustes = {
                navController.navigateToSettings()
            },
            onAmistades = {
                navController.navigateToFriends()
            },
            onEvent = { eventId ->
                navController.navigateToEventInfo(eventId)
            }
        )
        friendsScreen(
            onOpenProfile = { navController.navigateToPublicProfile(it) },
            onBackClick = {navController.navigateUp()},
            onHomeClick = {
                navController.navigateToHome(bottomBarOptions)
            },
            onSearchClick = {
                navController.navigateToSearch(bottomBarOptions)
            },
            onCreateClick = {
                navController.navigateToCreateEvent(bottomBarOptions)
            },
            onProfileClick = {
                navController.navigateUp()
            }
        )
        settingsScreen(
            onBackClick = {navController.navigateUp()},
            onLogoutClick = {
                navController.navigateToAuthNest(
                    navOptions {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                )
            },
            onHomeClick = {
                navController.navigateToHome(bottomBarOptions)
            },
            onSearchClick = {
                navController.navigateToSearch(bottomBarOptions)
            },
            onCreateClick = {
                navController.navigateToCreateEvent(bottomBarOptions)
            },
            onProfileClick = {
                navController.navigateUp()
            }
        )
    }
}
