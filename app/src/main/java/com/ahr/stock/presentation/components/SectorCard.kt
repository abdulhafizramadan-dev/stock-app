package com.ahr.stock.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.domain.model.SectorSummary

@Composable
fun SectorCard(
    sector: SectorSummary,
    modifier: Modifier = Modifier,
    onClick: ((SectorSummary) -> Unit)? = null,
) {
    val changeColor = when (sector.direction) {
        "up" -> Color(0xFF00C853)
        "down" -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val sign = if (sector.changePercent > 0) "+" else ""

    SectionCard(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick(sector) } else Modifier),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = sector.displayName,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "$sign${"%.2f".format(sector.changePercent)}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = changeColor,
            )
            Text(
                text = "${sector.stockCount} stocks",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

