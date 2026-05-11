package com.ahr.stock.domain.usecase.sector

import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.repository.SectorRepository
import com.ahr.stock.domain.usecase.UseCase

class GetSectorsSummaryUseCase(
    private val repository: SectorRepository,
) : UseCase<GetSectorsSummaryUseCase.Params, Result<List<SectorSummary>>> {

    data class Params(val region: String = "id")

    override suspend fun invoke(params: Params): Result<List<SectorSummary>> =
        repository.getSectorsSummary(params.region)
}

