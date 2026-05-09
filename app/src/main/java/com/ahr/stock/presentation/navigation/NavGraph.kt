package com.ahr.stock.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ahr.stock.presentation.screen.detail.DetailScreen
import com.ahr.stock.presentation.screen.home.HomeScreen

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToDetail = { ticker ->
                    navController.navigate(Screen.StockDetail(ticker).createRoute())
                },
            )
        }

        composable(
            route = Screen.StockDetail("").route,
            arguments = listOf(navArgument("ticker") { type = NavType.StringType }),
        ) { backStackEntry ->
            val ticker = backStackEntry.arguments?.getString("ticker") ?: return@composable
            DetailScreen(
                ticker = ticker,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

