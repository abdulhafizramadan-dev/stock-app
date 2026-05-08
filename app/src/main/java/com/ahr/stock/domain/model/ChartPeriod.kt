package com.ahr.stock.domain.model

enum class ChartPeriod(val period: String, val interval: String, val label: String) {
    ONE_DAY("1d", "1m", "1D"),
    ONE_WEEK("5d", "1h", "1W"),
    ONE_MONTH("1mo", "1d", "1M"),
    SIX_MONTHS("6mo", "1d", "6M"),
    ONE_YEAR("1y", "1wk", "1Y"),
    FIVE_YEARS("5y", "1mo", "5Y"),
}

