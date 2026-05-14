package com.ahr.stock.domain.usecase.sector

import com.ahr.stock.domain.model.SectorWithStocks
import com.ahr.stock.domain.repository.SectorRepository
import com.ahr.stock.domain.usecase.UseCase

class GetSectorStocksUseCase(
    private val repository: SectorRepository,
) : UseCase<GetSectorStocksUseCase.Params, Result<SectorWithStocks>> {

    data class Params(
        val sectorKey: String,
        val region: String = "id",
        val limit: Int = 50,
    )

    override suspend fun invoke(params: Params): Result<SectorWithStocks> =
        repository.getSectorStocks(params.sectorKey, params.region, params.limit)
}

