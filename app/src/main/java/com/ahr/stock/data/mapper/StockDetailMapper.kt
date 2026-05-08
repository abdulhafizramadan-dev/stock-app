package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.StockDetailResponseDto
import com.ahr.stock.domain.model.StockDetail

class StockDetailMapper : Mapper<StockDetailResponseDto, StockDetail> {
    override fun map(from: StockDetailResponseDto): StockDetail {
        val info = from.rawInfo
        return StockDetail(
            ticker = from.ticker,
            name = info.shortName ?: info.longName ?: from.ticker,
            currency = info.currency ?: "IDR",
            price = info.regularMarketPrice ?: 0.0,
            previousClose = info.regularMarketPreviousClose ?: 0.0,
            change = info.regularMarketChange ?: 0.0,
            changePercent = info.regularMarketChangePercent ?: 0.0,
            volume = info.regularMarketVolume ?: 0L,
            averageVolume = info.averageVolume ?: 0L,
            marketCap = info.marketCap ?: 0L,
            week52High = info.fiftyTwoWeekHigh ?: 0.0,
            week52Low = info.fiftyTwoWeekLow ?: 0.0,
            fiftyDayAverage = info.fiftyDayAverage ?: 0.0,
            twoHundredDayAverage = info.twoHundredDayAverage ?: 0.0,
            pe = info.trailingPE,
            forwardPe = info.forwardPE,
            dividendRate = info.dividendRate,
            dividendYield = info.dividendYield,
            beta = info.beta,
            bookValue = info.bookValue,
            priceToBook = info.priceToBook,
            sector = info.sector,
            industry = info.industry,
        )
    }
}

