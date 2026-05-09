package com.ahr.stock.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.domain.model.Stock

@Composable
fun StockRow(
    stock: Stock,
    onClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(stock.ticker) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stock.ticker,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
            Text(
                text = stock.name,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${"%.0f".format(stock.price)}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            PriceChip(changePercent = stock.changePercent)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StockRowPreview() {
    StockRow(
        stock = Stock(
            ticker = "BBCA",
            name = "Bank Central Asia",
            price = 9500.0,
            changePercent = 2.35,
            volume = 1_000_000L,
            marketCap = 1_000_000_000L,
        ),
        onClick = {},
    )
}

