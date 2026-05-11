package com.ahr.stock.domain.usecase.index

import com.ahr.stock.domain.model.IndexHistory
import com.ahr.stock.domain.repository.IndexRepository
import com.ahr.stock.domain.usecase.UseCase

class GetIndexHistoryUseCase(
    private val repository: IndexRepository,
) : UseCase<GetIndexHistoryUseCase.Params, Result<IndexHistory>> {

    data class Params(
        val symbol: String,
        val period: String = "1d",
        val interval: String = "1m",
        val limit: Int = Int.MAX_VALUE,
    )

    override suspend fun invoke(params: Params): Result<IndexHistory> =
        repository.getIndexHistory(params.symbol, params.period, params.interval, params.limit)
}
