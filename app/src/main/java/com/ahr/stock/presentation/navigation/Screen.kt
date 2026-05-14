package com.ahr.stock.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class StockDetail(val ticker: String) : Screen("detail/{ticker}") {
        fun createRoute() = "detail/$ticker"
    }
    data class SectorStocks(val sectorKey: String) : Screen("sector/{sectorKey}") {
        fun createRoute() = "sector/$sectorKey"
    }
}

