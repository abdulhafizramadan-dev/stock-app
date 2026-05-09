package com.ahr.stock.presentation.screen.detail

import com.ahr.stock.domain.model.ChartPeriod
import com.ahr.stock.domain.model.NewsItem
import com.ahr.stock.domain.model.OhlcvPoint
import com.ahr.stock.domain.model.StockDetail

sealed interface DetailIntent {
    data class LoadDetail(val ticker: String) : DetailIntent
    data object Refresh : DetailIntent
    data class ChangePeriod(val period: ChartPeriod) : DetailIntent
    data class OnChartDrag(val index: Int?) : DetailIntent
    data class OpenNewsArticle(val url: String) : DetailIntent
}

data class DetailState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val detail: StockDetail? = null,
    val history: List<OhlcvPoint> = emptyList(),
    val news: List<NewsItem> = emptyList(),
    val selectedPeriod: ChartPeriod = ChartPeriod.ONE_DAY,
    val draggedIndex: Int? = null,
    val error: String? = null,
)

sealed interface DetailEffect {
    data class OpenUrl(val url: String) : DetailEffect
    data class ShowSnackbar(val message: String) : DetailEffect
}

