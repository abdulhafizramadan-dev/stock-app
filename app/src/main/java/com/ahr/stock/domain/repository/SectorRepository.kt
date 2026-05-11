package com.ahr.stock.domain.repository

import com.ahr.stock.domain.model.SectorSummary

interface SectorRepository {
    suspend fun getSectorsSummary(region: String): Result<List<SectorSummary>>
}

