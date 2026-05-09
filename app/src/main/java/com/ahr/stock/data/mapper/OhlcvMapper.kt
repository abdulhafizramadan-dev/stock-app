package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.OhlcvDto
import com.ahr.stock.domain.model.OhlcvPoint

class OhlcvMapper : Mapper<OhlcvDto, OhlcvPoint> {
    override fun map(from: OhlcvDto) = OhlcvPoint(
        date = from.resolvedDate,
        open = from.open,
        high = from.high,
        low = from.low,
        close = from.close,
        volume = from.volume,
    )
}

