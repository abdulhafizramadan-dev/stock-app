package com.ahr.stock.domain.usecase.stock

import com.ahr.stock.domain.model.StockDetail
import com.ahr.stock.domain.repository.StockRepository
import com.ahr.stock.domain.usecase.UseCase

class GetStockDetailUseCase(
    private val repository: StockRepository,
) : UseCase<GetStockDetailUseCase.Params, Result<StockDetail>> {

    data class Params(val ticker: String)

    override suspend fun invoke(params: Params): Result<StockDetail> =
        repository.getStockDetail(params.ticker)
}

