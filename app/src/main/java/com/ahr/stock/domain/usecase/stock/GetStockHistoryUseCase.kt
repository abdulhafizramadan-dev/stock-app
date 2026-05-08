package com.ahr.stock.domain.usecase.stock

import com.ahr.stock.domain.model.OhlcvPoint
import com.ahr.stock.domain.repository.StockRepository
import com.ahr.stock.domain.usecase.UseCase

class GetStockHistoryUseCase(
    private val repository: StockRepository,
) : UseCase<GetStockHistoryUseCase.Params, Result<List<OhlcvPoint>>> {

    data class Params(
        val ticker: String,
        val period: String,
        val interval: String,
        val limit: Int = 30,
    )

    override suspend fun invoke(params: Params): Result<List<OhlcvPoint>> =
        repository.getStockHistory(params.ticker, params.period, params.interval, params.limit)
}

