package com.ahr.stock.data.remote.api

import com.ahr.stock.data.remote.dto.SectorStocksResponseDto
import com.ahr.stock.data.remote.dto.SectorSummaryResponseDto
import com.ahr.stock.data.remote.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SectorApiService(private val client: HttpClient) {

    suspend fun getSectorsSummary(region: String = "id"): Result<SectorSummaryResponseDto> =
        safeApiCall {
            client.get("sectors/summary") {
                parameter("region", region)
            }.body()
        }

    suspend fun getSectorStocks(
        sectorKey: String,
        region: String = "id",
        limit: Int = 50,
    ): Result<SectorStocksResponseDto> =
        safeApiCall {
            client.get("sectors/$sectorKey/stocks") {
                parameter("region", region)
                parameter("limit", limit)
            }.body()
        }
}

