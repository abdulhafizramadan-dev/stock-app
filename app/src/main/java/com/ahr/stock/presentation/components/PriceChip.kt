package com.ahr.stock.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GreenBackground = Color(0xFF1B4332)
private val GreenText = Color(0xFF00C853)
private val RedBackground = Color(0xFF4A0E0E)
private val RedText = Color(0xFFE53935)

@Composable
fun PriceChip(
    changePercent: Double,
    modifier: Modifier = Modifier,
) {
    val isPositive = changePercent >= 0
    val backgroundColor = if (isPositive) GreenBackground else RedBackground
    val textColor = if (isPositive) GreenText else RedText
    val sign = if (isPositive) "+" else ""
    val label = "$sign${"%.2f".format(changePercent)}%"

    Text(
        text = label,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(color = backgroundColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun PriceChipPreview() {
    PriceChip(changePercent = 3.45)
}

