package com.ahr.stock.domain.repository

import com.ahr.stock.domain.model.SectorSummary
import com.ahr.stock.domain.model.SectorWithStocks

interface SectorRepository {
    suspend fun getSectorsSummary(region: String): Result<List<SectorSummary>>
    suspend fun getSectorStocks(sectorKey: String, region: String, limit: Int): Result<SectorWithStocks>
}

