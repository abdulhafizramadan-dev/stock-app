package com.ahr.stock.data.repository

import com.ahr.stock.data.mapper.IndexPointMapper
import com.ahr.stock.data.remote.api.IndexApiService
import com.ahr.stock.domain.model.IndexHistory
import com.ahr.stock.domain.repository.IndexRepository

class IndexRepositoryImpl(
    private val apiService: IndexApiService,
    private val indexPointMapper: IndexPointMapper,
) : IndexRepository {

    override suspend fun getIndexHistory(
        symbol: String,
        period: String,
        interval: String,
        limit: Int,
    ): Result<IndexHistory> =
        apiService.getIndexHistory(symbol, period, interval, limit).map { dto ->
            IndexHistory(
                points = indexPointMapper.mapList(dto.data),
                previousClose = dto.previousClose,
            )
        }
}
