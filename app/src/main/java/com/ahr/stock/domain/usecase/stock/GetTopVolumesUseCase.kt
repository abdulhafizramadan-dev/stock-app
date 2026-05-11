package com.ahr.stock.domain.usecase.stock

import com.ahr.stock.domain.model.Stock
import com.ahr.stock.domain.repository.StockRepository
import com.ahr.stock.domain.usecase.UseCase

class GetTopVolumesUseCase(
    private val repository: StockRepository,
) : UseCase<GetTopVolumesUseCase.Params, Result<List<Stock>>> {

    data class Params(val limit: Int = 10)

    override suspend fun invoke(params: Params): Result<List<Stock>> =
        repository.getTopVolumes(params.limit)
}

