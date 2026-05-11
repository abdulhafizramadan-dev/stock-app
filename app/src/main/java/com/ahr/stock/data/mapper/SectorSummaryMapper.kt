package com.ahr.stock.data.mapper

import com.ahr.stock.data.remote.dto.SectorDto
import com.ahr.stock.domain.model.SectorSummary

class SectorSummaryMapper : Mapper<SectorDto, SectorSummary> {
    override fun map(from: SectorDto) = SectorSummary(
        name = from.name,
        key = from.key,
        displayName = from.displayName,
        changePercent = from.changePercent,
        stockCount = from.stockCount,
        direction = from.direction,
    )
}

