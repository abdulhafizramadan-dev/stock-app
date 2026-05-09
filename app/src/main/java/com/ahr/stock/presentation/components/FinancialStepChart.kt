package com.ahr.stock.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ahr.stock.utils.formatCrosshairLabel
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

private val ChartHeight = 200.dp
private val LabelPadding = 20.dp
private const val DashOn = 10f
private const val DashOff = 10f

data class ChartPoint(val x: Double, val y: Double)

@Composable
fun <T> FinancialStepChart(
    data: List<T>,
    xSelector: (T) -> String,
    ySelector: (T) -> Double,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 2.dp,
    backgroundColor: Color = Color.Transparent,
    bullishColor: Color = Color(0xFF00C853),
    bearishColor: Color = Color(0xFFE53935),
    gridColor: Color = Color(0xFFBDBBBB),
    baselineValue: Double? = null,
    labelFontSize: TextUnit = 11.sp,
    showYAxisLabels: Boolean = true,
    dragEnabled: Boolean = true,
    onDragIndexChange: ((index: Int?) -> Unit)? = null,
) {
    if (data.size < 2) return

    val xLabels = data.map { xSelector(it) }
    val dataPoints = data.mapIndexed { index, item -> ChartPoint(index.toDouble(), ySelector(item)) }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = remember(labelFontSize) { TextStyle(fontSize = labelFontSize) }
    val hapticFeedback = LocalHapticFeedback.current
    var crosshairX by remember { mutableStateOf<Float?>(null) }

    val dynamicGutterWidth = remember(dataPoints, labelFontSize, showYAxisLabels) {
        if (!showYAxisLabels) return@remember 0f
        val minY = dataPoints.minOf { it.y }
        val maxY = dataPoints.maxOf { it.y }
        val yRange = if (maxY == minY) 1.0 else maxY - minY
        val rawStep = yRange / 4.0
        val magnitude = 10.0.pow(floor(log10(abs(rawStep))))
        val step = if (rawStep > 0) ceil(rawStep / magnitude) * magnitude else 1.0
        val labelValues = mutableSetOf<Double>()
        var v = floor(minY / step) * step
        while (v <= maxY + step * 0.5) {
            if (v >= minY - step * 0.1 && v <= maxY + step * 0.1) labelValues.add(v)
            v += step
        }
        val maxWidth = labelValues.maxOfOrNull { value ->
            textMeasurer.measure(formatChartLabel(value), labelStyle).size.width
        } ?: 0
        maxWidth.toFloat()
    }

    val dragModifier = if (dragEnabled) {
        Modifier.pointerInput(dataPoints) {
            awaitEachGesture {
                val down = awaitFirstDown()
                hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                crosshairX = down.position.x
                do {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull() ?: break
                    if (change.pressed) {
                        crosshairX = change.position.x
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                        change.consume()
                    }
                } while (event.changes.any { it.pressed })
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                crosshairX = null
                onDragIndexChange?.invoke(null)
            }
        }
    } else {
        Modifier
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(ChartHeight)
            .then(dragModifier),
    ) {
        val gutterPx = if (showYAxisLabels) dynamicGutterWidth + 4.dp.toPx() else 0f
        val strokePx = strokeWidth.toPx()
        val labelPaddingPx = LabelPadding.toPx()
        val chartWidth = size.width - gutterPx
        val chartHeight = size.height - labelPaddingPx * 2

        if (backgroundColor != Color.Transparent) {
            drawRect(color = backgroundColor, size = size)
        }

        val minY = dataPoints.minOf { it.y }
        val maxY = dataPoints.maxOf { it.y }
        val minX = dataPoints.minOf { it.x }
        val maxX = dataPoints.maxOf { it.x }

        val yRange = if (maxY == minY) 1.0 else maxY - minY
        val xRange = if (maxX == minX) 1.0 else maxX - minX

        fun xToCanvas(x: Double) = ((x - minX) / xRange * chartWidth).toFloat()
        fun yToCanvas(y: Double) = labelPaddingPx + ((maxY - y) / yRange * chartHeight).toFloat()

        val xStep = chartWidth / (dataPoints.size - 1).coerceAtLeast(1)
        val dynamicCornerRadius = minOf(xStep * 0.15f, 8.dp.toPx())

        val baseline = baselineValue ?: minY
        val baselineY = yToCanvas(baseline)

        drawBaselineDashed(y = baselineY, chartWidth = chartWidth, strokePx = strokePx, gridColor = gridColor)

        drawRoundedStaircaseLine(
            points = dataPoints,
            xToCanvas = ::xToCanvas,
            yToCanvas = ::yToCanvas,
            baseline = baseline,
            bullishColor = bullishColor,
            bearishColor = bearishColor,
            strokePx = strokePx,
            cornerPx = dynamicCornerRadius,
        )

        val maxPoint = dataPoints.maxBy { it.y }
        val minPoint = dataPoints.minBy { it.y }
        val lastPoint = dataPoints.last()
        val lineColor = if (lastPoint.y >= baseline) bullishColor else bearishColor

        crosshairX?.let { rawX ->
            if (dragEnabled) {
                val clampedX = rawX.coerceIn(0f, chartWidth)
                val nearestIndex = dataPoints.indices.minBy { abs(xToCanvas(dataPoints[it].x) - clampedX) }
                val nearestPoint = dataPoints[nearestIndex]
                val snapX = xToCanvas(nearestPoint.x)
                val snapY = yToCanvas(nearestPoint.y)

                onDragIndexChange?.invoke(nearestIndex)

                drawCrosshair(
                    snapX = snapX,
                    snapY = snapY,
                    chartHeight = size.height,
                    chartWidth = chartWidth,
                    lineColor = lineColor,
                    xLabel = xLabels[nearestIndex],
                    textMeasurer = textMeasurer,
                    labelStyle = labelStyle,
                    gridColor = gridColor,
                )
            }
        }

        if (crosshairX == null) {
            drawPeakValleyCallout(
                value = maxY,
                canvasX = xToCanvas(maxPoint.x),
                canvasY = yToCanvas(maxY),
                color = lineColor,
                above = true,
                chartWidth = chartWidth,
                textMeasurer = textMeasurer,
                labelStyle = labelStyle,
            )

            drawPeakValleyCallout(
                value = minY,
                canvasX = xToCanvas(minPoint.x),
                canvasY = yToCanvas(minY),
                color = lineColor,
                above = false,
                chartWidth = chartWidth,
                textMeasurer = textMeasurer,
                labelStyle = labelStyle,
            )
        }

        if (showYAxisLabels) {
            drawYAxisLabels(
                minY = minY,
                maxY = maxY,
                chartWidth = chartWidth,
                textMeasurer = textMeasurer,
                labelStyle = labelStyle,
                yToCanvas = ::yToCanvas,
                gridColor = gridColor,
            )
        }
    }
}

private fun DrawScope.drawBaselineDashed(y: Float, chartWidth: Float, strokePx: Float, gridColor: Color) {
    drawLine(
        color = gridColor,
        start = Offset(0f, y),
        end = Offset(chartWidth, y),
        strokeWidth = strokePx,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn, DashOff)),
        cap = StrokeCap.Round,
    )
}

private fun DrawScope.drawRoundedStaircaseLine(
    points: List<ChartPoint>,
    xToCanvas: (Double) -> Float,
    yToCanvas: (Double) -> Float,
    baseline: Double,
    bullishColor: Color,
    bearishColor: Color,
    strokePx: Float,
    cornerPx: Float,
) {
    val lastPoint = points.last()
    val isBullish = lastPoint.y >= baseline
    val lineColor = if (isBullish) bullishColor else bearishColor

    val firstX = xToCanvas(points.first().x)
    val firstY = yToCanvas(points.first().y)
    val lastX = xToCanvas(lastPoint.x)
    val lastY = yToCanvas(lastPoint.y)

    val staircasePath = Path().apply {
        moveTo(firstX, firstY)
        for (i in 0 until points.size - 1) {
            val x1 = xToCanvas(points[i + 1].x)
            val y0 = yToCanvas(points[i].y)
            val y1 = yToCanvas(points[i + 1].y)
            lineTo(x1, y0)
            lineTo(x1, y1)
        }
    }

    val areaPath = Path().apply {
        addPath(staircasePath)
        lineTo(lastX, size.height)
        lineTo(firstX, size.height)
        close()
    }

    val staircaseTopY = minOf(firstY, lastY)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(lineColor.copy(alpha = 0.35f), Color.Transparent),
        startY = staircaseTopY,
        endY = size.height,
    )

    drawPath(path = areaPath, brush = gradientBrush)

    drawPath(
        path = staircasePath,
        color = lineColor,
        style = Stroke(
            width = strokePx,
            cap = StrokeCap.Round,
            pathEffect = PathEffect.cornerPathEffect(cornerPx),
        ),
    )
}

private fun DrawScope.drawPeakValleyCallout(
    value: Double,
    canvasX: Float,
    canvasY: Float,
    color: Color,
    above: Boolean,
    chartWidth: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
) {
    val text = formatChartLabel(value)
    val measured = textMeasurer.measure(text, labelStyle)
    val offset = 6.dp.toPx()

    val textX = (canvasX - measured.size.width / 2f).coerceIn(0f, chartWidth - measured.size.width)
    val textY = if (above) {
        (canvasY - measured.size.height - offset).coerceAtLeast(0f)
    } else {
        (canvasY + offset).coerceAtMost(size.height - measured.size.height)
    }

    drawText(
        textMeasurer = textMeasurer,
        text = text,
        style = labelStyle.copy(color = color),
        topLeft = Offset(textX, textY),
    )
}

private fun DrawScope.drawCrosshair(
    snapX: Float,
    snapY: Float,
    chartHeight: Float,
    chartWidth: Float,
    lineColor: Color,
    xLabel: String,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    gridColor: Color,
) {
    val formattedLabel = formatCrosshairLabel(xLabel)
    val measured = textMeasurer.measure(formattedLabel, labelStyle)
    val labelX = (snapX - measured.size.width / 2f).coerceIn(0f, chartWidth - measured.size.width)
    val labelHeight = measured.size.height.toFloat()
    val lineGap = 4.dp.toPx()

    drawText(
        textMeasurer = textMeasurer,
        text = formattedLabel,
        style = labelStyle.copy(color = gridColor),
        topLeft = Offset(labelX, 0f),
    )

    drawLine(
        color = gridColor,
        start = Offset(snapX, labelHeight + lineGap),
        end = Offset(snapX, chartHeight),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f)),
    )

    drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(snapX, snapY))
}

private fun DrawScope.drawYAxisLabels(
    minY: Double,
    maxY: Double,
    chartWidth: Float,
    textMeasurer: TextMeasurer,
    labelStyle: TextStyle,
    yToCanvas: (Double) -> Float,
    gridColor: Color,
) {
    val yRange = maxY - minY
    val rawStep = yRange / 4.0
    val magnitude = 10.0.pow(floor(log10(abs(rawStep))))
    val step = if (rawStep > 0) ceil(rawStep / magnitude) * magnitude else 1.0

    val labelValues = mutableSetOf<Double>()
    var v = floor(minY / step) * step
    while (v <= maxY + step * 0.5) {
        if (v >= minY - step * 0.1 && v <= maxY + step * 0.1) labelValues.add(v)
        v += step
    }

    val labelX = chartWidth + 4.dp.toPx()

    for (value in labelValues) {
        val cy = yToCanvas(value)
        if (cy < 0f || cy > size.height) continue
        val text = formatChartLabel(value)
        val measured = textMeasurer.measure(text, labelStyle)

        val textY = (cy - measured.size.height / 2f).coerceIn(0f, size.height - measured.size.height)
        drawText(
            textMeasurer = textMeasurer,
            text = text,
            style = labelStyle.copy(color = gridColor),
            topLeft = Offset(labelX, textY),
        )
    }
}

private fun formatChartLabel(value: Double): String {
    return if (value == floor(value)) value.toLong().toString()
    else {
        val rounded = (value * 10).roundToInt()
        val intPart = rounded / 10
        val decPart = abs(rounded % 10)
        "$intPart.$decPart"
    }
}


@Preview(showBackground = true)
@Composable
private fun FinancialStepChartPreview() {
    data class SamplePoint(val date: String, val close: Double)
    val sampleData = listOf(
        SamplePoint("2026-01-01", 7200.0),
        SamplePoint("2026-02-01", 7350.0),
        SamplePoint("2026-03-01", 7100.0),
        SamplePoint("2026-04-01", 7500.0),
        SamplePoint("2026-05-01", 7450.0),
    )
    FinancialStepChart(
        data = sampleData,
        xSelector = { it.date },
        ySelector = { it.close },
        baselineValue = sampleData.first().close,
    )
}
