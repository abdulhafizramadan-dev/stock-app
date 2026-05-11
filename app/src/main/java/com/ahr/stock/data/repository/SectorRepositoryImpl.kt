package com.ahr.stock.data.repository

import com.ahr.stock.data.mapper.SectorSummaryMapper
import com.ahr.stock.data.remote.api.SectorApiService
import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.repository.SectorRepository

class SectorRepositoryImpl(
    private val apiService: SectorApiService,
    private val mapper: SectorSummaryMapper,
) : SectorRepository {

    override suspend fun getSectorsSummary(region: String): Result<List<SectorSummary>> =
        apiService.getSectorsSummary(region).map { mapper.mapList(it.sectors) }
}

