package com.ahr.stock.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahr.stock.domain.usecase.index.GetIndexHistoryUseCase
import com.ahr.stock.domain.usecase.news.GetHighlightedNewsUseCase
import com.ahr.stock.domain.usecase.stock.GetGainersUseCase
import com.ahr.stock.domain.usecase.stock.GetLosersUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getGainers: GetGainersUseCase,
    private val getLosers: GetLosersUseCase,
    private val getIndexHistory: GetIndexHistoryUseCase,
    private val getHighlightedNews: GetHighlightedNewsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<HomeEffect>()
    val effect: SharedFlow<HomeEffect> = _effect.asSharedFlow()

    init {
        onIntent(HomeIntent.LoadMarket)
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadMarket -> loadMarket(isRefresh = false)
            is HomeIntent.Refresh -> loadMarket(isRefresh = true)
            is HomeIntent.SelectStock -> navigateToDetail(intent.ticker)
            is HomeIntent.SelectTab -> _state.update { it.copy(selectedTab = intent.tab) }
            is HomeIntent.OnChartDrag -> _state.update { it.copy(draggedIndex = intent.index) }
            is HomeIntent.OpenNewsArticle -> openUrl(intent.url)
        }
    }

    private fun loadMarket(isRefresh: Boolean) {
        viewModelScope.launch {
            _state.update {
                if (isRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val gainersDeferred = async { getGainers(GetGainersUseCase.Params(limit = 5)) }
            val losersDeferred = async { getLosers(GetLosersUseCase.Params(limit = 5)) }
            val indexDeferred = async {
                getIndexHistory(
                    GetIndexHistoryUseCase.Params(symbol = "^JKSE", period = "1d", interval = "1m")
                )
            }
            val newsDeferred = async { getHighlightedNews(GetHighlightedNewsUseCase.Params(count = 5)) }

            val gainersResult = gainersDeferred.await()
            val losersResult = losersDeferred.await()
            val indexResult = indexDeferred.await()
            val newsResult = newsDeferred.await()

            val error = gainersResult.exceptionOrNull()
                ?: losersResult.exceptionOrNull()
                ?: indexResult.exceptionOrNull()

            if (error != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Something went wrong",
                    )
                }
                _effect.emit(HomeEffect.ShowSnackbar(error.message ?: "Something went wrong"))
                return@launch
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    gainers = gainersResult.getOrDefault(emptyList()),
                    losers = losersResult.getOrDefault(emptyList()),
                    indexPoints = indexResult.getOrDefault(emptyList()),
                    news = newsResult.getOrDefault(emptyList()),
                    error = null,
                )
            }
        }
    }

    private fun navigateToDetail(ticker: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.NavigateToDetail(ticker))
        }
    }

    private fun openUrl(url: String) {
        viewModelScope.launch {
            _effect.emit(HomeEffect.OpenUrl(url))
        }
    }
}
