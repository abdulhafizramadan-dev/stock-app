package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.StockDto
import com.ahr.stock.domain.model.Stock

class StockMapper : Mapper<StockDto, Stock> {
    override fun map(from: StockDto) = Stock(
        ticker = from.ticker,
        name = from.name,
        price = from.price,
        changePercent = from.changePercent,
        volume = from.volume,
        marketCap = from.marketCap,
    )
}

