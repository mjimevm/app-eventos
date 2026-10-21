package plat.proyecto.guatevivo.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.navOptions
import plat.proyecto.guatevivo.paginas.event.ui.create.createEventScreen
import plat.proyecto.guatevivo.paginas.event.ui.create.navigateToCreateEvent
import plat.proyecto.guatevivo.paginas.event.ui.info.eventInfoScreen
import plat.proyecto.guatevivo.paginas.event.ui.info.navigateToEventInfo
import plat.proyecto.guatevivo.paginas.home.ui.home.HomeDestination
import plat.proyecto.guatevivo.paginas.home.ui.home.homeScreen
import plat.proyecto.guatevivo.paginas.home.ui.home.navigateToHome
import plat.proyecto.guatevivo.paginas.home.ui.search.navigateToSearch
import plat.proyecto.guatevivo.paginas.home.ui.search.searchScreen
import plat.proyecto.guatevivo.paginas.login.ui.AuthNavHost
import plat.proyecto.guatevivo.paginas.login.ui.AuthNestNav
import plat.proyecto.guatevivo.paginas.profile.navigateToProfileNest
import plat.proyecto.guatevivo.paginas.profile.profileNestNav

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    val bottomBarOptions = navOptions {
        popUpTo<HomeDestination> {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    val onHome: () -> Unit = {
        navController.navigateToHome(bottomBarOptions)
    }

    val onSearch: () -> Unit = {
        navController.navigateToSearch(bottomBarOptions)
    }

    val onCreate: () -> Unit = {
        navController.navigateToCreateEvent(bottomBarOptions)
    }

    val onProfile: () -> Unit = {
        navController.navigateToProfileNest(bottomBarOptions)
    }


    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AuthNavHost,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AuthNestNav(navController)
            profileNestNav(
                navController,
                isAccountBusiness = false, //TODO cambiar a variable segun el tipo de usuario
                bottomBarOptions = bottomBarOptions
            )
            homeScreen(
                onHomeClick = {},
                onSearchClick = onSearch,
                onCreateClick = onCreate,
                onProfileClick = onProfile,
                onEvent = {
                    navController.navigateToEventInfo()
                }
            )
            searchScreen(
                onHomeClick = onHome,
                onSearchClick = {},
                onCreateClick = onCreate,
                onProfileClick = onProfile,
                onEvent = {
                    navController.navigateToEventInfo()
                }
            )
            createEventScreen(
                onHomeClick = onHome,
                onSearchClick = onSearch,
                onCreateClick = {},
                onProfileClick = onProfile
            )
            eventInfoScreen(
                onHomeClick = onHome,
                onSearchClick = onSearch,
                onCreateClick = onCreate,
                onProfileClick = onProfile,
                onBackArrow = {
                    navController.navigateUp()
                }
            )
        }
    }
}