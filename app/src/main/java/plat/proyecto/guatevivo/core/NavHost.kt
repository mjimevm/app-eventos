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
import plat.proyecto.guatevivo.paginas.login.ui.onboarding.onBoardingDestination
import plat.proyecto.guatevivo.paginas.login.ui.onboarding.onBoardingScreen

@Composable
fun NavigationHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = onBoardingDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            onBoardingScreen()
        }
    }
}