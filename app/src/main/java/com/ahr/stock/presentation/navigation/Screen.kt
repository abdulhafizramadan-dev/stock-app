package com.ahr.stock.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class StockDetail(val ticker: String) : Screen("detail/{ticker}") {
        fun createRoute() = "detail/$ticker"
    }
}

