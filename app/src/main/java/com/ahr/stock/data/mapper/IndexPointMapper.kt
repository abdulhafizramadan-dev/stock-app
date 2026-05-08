package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.IndexDataPointDto
import com.ahr.stock.domain.model.IndexPoint

class IndexPointMapper : Mapper<IndexDataPointDto, IndexPoint> {
    override fun map(from: IndexDataPointDto) = IndexPoint(
        datetime = from.datetime,
        open = from.open,
        high = from.high,
        low = from.low,
        close = from.close,
        volume = from.volume,
        change = from.change,
        changePercent = from.changePercent,
    )
}

