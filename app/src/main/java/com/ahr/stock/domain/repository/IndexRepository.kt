package com.ahr.stock.domain.repository

import com.ahr.stock.domain.model.IndexPoint

interface IndexRepository {
    suspend fun getIndexHistory(symbol: String, period: String, interval: String, limit: Int): Result<List<IndexPoint>>
}

