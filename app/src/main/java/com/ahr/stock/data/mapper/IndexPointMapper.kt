package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.OhlcvDto
import com.ahr.stock.domain.model.IndexPoint

class IndexPointMapper : Mapper<OhlcvDto, IndexPoint> {
    override fun map(from: OhlcvDto) = IndexPoint(
        datetime = from.resolvedDate,
        open = from.open,
        high = from.high,
        low = from.low,
        close = from.close,
        volume = from.volume,
    )
}

