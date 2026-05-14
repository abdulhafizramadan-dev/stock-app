package com.ahr.stock.data.repository

import com.ahr.stock.data.mapper.SectorSummaryMapper
import com.ahr.stock.data.mapper.StockMapper
import com.ahr.stock.data.remote.api.SectorApiService
import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.model.SectorWithStocks
import com.ahr.stock.domain.repository.SectorRepository

class SectorRepositoryImpl(
    private val apiService: SectorApiService,
    private val sectorSummaryMapper: SectorSummaryMapper,
    private val stockMapper: StockMapper,
) : SectorRepository {

    override suspend fun getSectorsSummary(region: String): Result<List<SectorSummary>> =
        apiService.getSectorsSummary(region).map { sectorSummaryMapper.mapList(it.sectors) }

    override suspend fun getSectorStocks(sectorKey: String, region: String, limit: Int): Result<SectorWithStocks> =
        apiService.getSectorStocks(sectorKey, region, limit).map { response ->
            SectorWithStocks(
                sectorDisplayName = response.sector.displayName,
                stocks = stockMapper.mapList(response.stocks),
            )
        }
}

