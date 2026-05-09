package com.ahr.stock.presentation.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.domain.model.ChartPeriod

private val TabWidth = 44.dp
private val TabHeight = 40.dp
private val IndicatorThickness = 2.dp

@Composable
fun PeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = ChartPeriod.entries
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
    ) {
        periods.forEach { period ->
            PeriodTab(
                label = period.label,
                selected = period == selectedPeriod,
                onClick = { onPeriodSelected(period) },
            )
        }
    }
}

@Composable
private fun PeriodTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val indicatorColor = MaterialTheme.colorScheme.primary
    val textColor = if (selected) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(TabWidth)
            .height(TabHeight)
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick,
            )
            .drawBehind {
                if (selected) {
                    drawLine(
                        color = indicatorColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = IndicatorThickness.toPx(),
                    )
                }
            },
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PeriodSelectorPreview() {
    PeriodSelector(
        selectedPeriod = ChartPeriod.ONE_MONTH,
        onPeriodSelected = {},
    )
}
