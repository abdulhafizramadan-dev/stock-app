package com.ahr.stock.presentation.screen.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahr.stock.domain.model.ChartPeriod
import com.ahr.stock.domain.usecase.stock.GetStockDetailUseCase
import com.ahr.stock.domain.usecase.stock.GetStockHistoryUseCase
import com.ahr.stock.domain.usecase.stock.GetStockNewsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetailViewModel(
    private val getStockDetail: GetStockDetailUseCase,
    private val getStockHistory: GetStockHistoryUseCase,
    private val getStockNews: GetStockNewsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<DetailEffect>()
    val effect: SharedFlow<DetailEffect> = _effect.asSharedFlow()

    private var currentTicker: String = ""

    fun onIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadDetail -> {
                currentTicker = intent.ticker
                loadAll(ticker = intent.ticker, isRefresh = false)
            }
            is DetailIntent.Refresh -> loadAll(ticker = currentTicker, isRefresh = true)
            is DetailIntent.ChangePeriod -> changePeriod(intent.period)
            is DetailIntent.OnChartDrag -> _state.update { it.copy(draggedIndex = intent.index) }
            is DetailIntent.OpenNewsArticle -> openUrl(intent.url)
        }
    }

    private fun loadAll(ticker: String, isRefresh: Boolean) {
        viewModelScope.launch {
            val period = _state.value.selectedPeriod
            _state.update {
                if (isRefresh) it.copy(isRefreshing = true, error = null)
                else it.copy(isLoading = true, error = null)
            }

            val detailDeferred = async {
                getStockDetail(GetStockDetailUseCase.Params(ticker))
            }
            val historyDeferred = async {
                getStockHistory(
                    GetStockHistoryUseCase.Params(
                        ticker = ticker,
                        period = period.period,
                        interval = period.interval,
                    )
                )
            }
            val newsDeferred = async {
                getStockNews(GetStockNewsUseCase.Params(ticker = ticker, count = 10))
            }

            val detailResult = detailDeferred.await()
            val historyResult = historyDeferred.await()
            val newsResult = newsDeferred.await()

            val error = detailResult.exceptionOrNull()
                ?: historyResult.exceptionOrNull()
                ?: newsResult.exceptionOrNull()

            if (error != null) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = error.message ?: "Something went wrong",
                    )
                }
                _effect.emit(DetailEffect.ShowSnackbar(error.message ?: "Something went wrong"))
                return@launch
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    detail = detailResult.getOrNull(),
                    history = historyResult.getOrDefault(emptyList()),
                    news = newsResult.getOrDefault(emptyList()),
                    error = null,
                )
            }
        }
    }

    private fun changePeriod(period: ChartPeriod) {
        _state.update { it.copy(selectedPeriod = period, draggedIndex = null) }
        viewModelScope.launch {
            val result = getStockHistory(
                GetStockHistoryUseCase.Params(
                    ticker = currentTicker,
                    period = period.period,
                    interval = period.interval,
                )
            )
            result.fold(
                onSuccess = { points ->
                    _state.update { it.copy(history = points) }
                },
                onFailure = { error ->
                    val message = error.message ?: "Failed to load history"
                    _state.update { it.copy(error = message) }
                    _effect.emit(DetailEffect.ShowSnackbar(message))
                },
            )
        }
    }

    private fun openUrl(url: String) {
        viewModelScope.launch {
            _effect.emit(DetailEffect.OpenUrl(url))
        }
    }
}

