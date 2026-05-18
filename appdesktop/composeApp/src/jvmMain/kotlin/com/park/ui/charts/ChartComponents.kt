package com.park.ui.charts

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

// ─────────────────────────────────────────────
// Design tokens
// ─────────────────────────────────────────────

object ChartColors {
    val Blue400    = Color(0xFF378ADD)
    val Blue200    = Color(0xFF85B7EB)
    val Blue50     = Color(0xFFE6F1FB)
    val Teal400    = Color(0xFF1D9E75)
    val Teal50     = Color(0xFFE1F5EE)
    val Amber400   = Color(0xFFEF9F27)
    val Amber50    = Color(0xFFFAEEDA)
    val Red400     = Color(0xFFE24B4A)
    val Green400   = Color(0xFF639922)
    val Green50    = Color(0xFFEAF3DE)
    val Gray400    = Color(0xFF888780)
    val Gray100    = Color(0xFFD3D1C7)
    val Gray50     = Color(0xFFF1EFE8)
    val TextPrimary   = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF888780)
    val GridLine      = Color(0x1A888780)
    val Surface       = Color(0xFFFAFAF8)
}

// ─────────────────────────────────────────────
// Bar Chart with compare line overlay
// ─────────────────────────────────────────────

data class BarChartData(
    val labels: List<String>,
    val current: List<Float>,
    val previous: List<Float>? = null,
    val barColor: Color = ChartColors.Blue400,
    val lineColor: Color = ChartColors.Gray400,
    val yAxisLabel: (Float) -> String = { it.toString() }
)

@Composable
fun BarCompareChart(
    data: BarChartData,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    tooltipTitle: (Int) -> String = { data.labels.getOrElse(it) { "" } },
    tooltipBody: (Int, Float) -> String = { _, v -> v.toInt().toString() },
    tooltipPrevBody: (Int, Float) -> String = { _, v -> "Trước: ${v.toInt()}" },
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_trigger"
    )
    LaunchedEffect(data) { trigger = false; trigger = true }

    val labelStyle = TextStyle(
        fontSize = 10.sp,
        color = ChartColors.TextSecondary,
        fontWeight = FontWeight.Normal
    )
    val axisStyle = TextStyle(
        fontSize = 9.sp,
        color = ChartColors.TextSecondary
    )

    val padLeftDp = 44.dp
    val padRightDp = 16.dp
    val padTopDp = 12.dp
    val padBottomDp = 28.dp

    val n = data.current.size
    val niceMaxVal = remember(data) {
        niceMax((data.current + (data.previous ?: emptyList())).maxOrNull() ?: 1f)
    }

    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(data, canvasSize) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move,
                                PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val padLeft = with(density) { padLeftDp.toPx() }
                                    val padRight = with(density) { padRightDp.toPx() }
                                    val padTop = with(density) { padTopDp.toPx() }
                                    val padBottom = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - padLeft - padRight
                                    val ch = canvasSize.height - padTop - padBottom
                                    val inside = pos.x in (padLeft - 8f)..(padLeft + cw + 8f) &&
                                                 pos.y in (padTop - 8f)..(padTop + ch + 8f)
                                    if (cw > 0f && inside && n > 0) {
                                        val groupW = cw / n
                                        val idx = ((pos.x - padLeft) / groupW).toInt().coerceIn(0, n - 1)
                                        hoverIdx = idx
                                    } else {
                                        hoverIdx = null
                                    }
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val padLeft   = padLeftDp.toPx()
            val padRight  = padRightDp.toPx()
            val padTop    = padTopDp.toPx()
            val padBottom = padBottomDp.toPx()
            val chartW = size.width - padLeft - padRight
            val chartH = size.height - padTop - padBottom

            val gridCount = 4
            repeat(gridCount + 1) { i ->
                val y = padTop + chartH * (1f - i.toFloat() / gridCount)
                val v = niceMaxVal * i / gridCount
                drawLine(
                    color = ChartColors.GridLine,
                    start = Offset(padLeft, y),
                    end = Offset(padLeft + chartW, y),
                    strokeWidth = 0.5.dp.toPx()
                )
                val label = textMeasurer.measure(data.yAxisLabel(v), axisStyle)
                drawText(label, topLeft = Offset(padLeft - label.size.width - 6.dp.toPx(), y - label.size.height / 2))
            }

            if (n == 0 || chartW <= 0f || chartH <= 0f) return@Canvas
            val groupW = chartW / n
            val barW = groupW * 0.52f
            val cornerR = 4.dp.toPx()

            data.current.forEachIndexed { i, value ->
                val barH = (value / niceMaxVal) * chartH * animProg
                val x = padLeft + i * groupW + (groupW - barW) / 2
                val top = padTop + chartH - barH

                drawRect(
                    color = data.barColor.copy(alpha = 0.07f),
                    topLeft = Offset(x, padTop),
                    size = Size(barW, chartH)
                )
                drawRoundRect(
                    color = data.barColor,
                    topLeft = Offset(x, top),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(cornerR, cornerR)
                )

                val lbl = textMeasurer.measure(data.labels[i], labelStyle)
                drawText(lbl, topLeft = Offset(
                    padLeft + i * groupW + groupW / 2 - lbl.size.width / 2,
                    padTop + chartH + 6.dp.toPx()
                ))
            }

            data.previous?.let { prev ->
                val pts = prev.mapIndexed { i, v ->
                    val x = padLeft + i * groupW + groupW / 2
                    val y = padTop + chartH - (v / niceMaxVal) * chartH * animProg
                    Offset(x, y)
                }
                val visibleCount = (pts.size * animProg).toInt().coerceAtLeast(1)
                val visiblePts = pts.take(visibleCount)

                val path = Path()
                visiblePts.forEachIndexed { i, pt ->
                    if (i == 0) path.moveTo(pt.x, pt.y)
                    else {
                        val prev2 = visiblePts[i - 1]
                        val cx = (prev2.x + pt.x) / 2
                        path.cubicTo(cx, prev2.y, cx, pt.y, pt.x, pt.y)
                    }
                }
                drawPath(
                    path = path,
                    color = data.lineColor.copy(alpha = 0.6f),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
                    )
                )
                visiblePts.forEach { pt ->
                    drawCircle(color = ChartColors.Surface, radius = 3.5.dp.toPx(), center = pt)
                    drawCircle(color = data.lineColor, radius = 2.5.dp.toPx(), center = pt,
                        style = Stroke(width = 1.5.dp.toPx()))
                }
            }

            hoverIdx?.let { idx ->
                if (idx in 0 until n) {
                    val centerX = padLeft + idx * groupW + groupW / 2
                    drawLine(
                        color = data.barColor.copy(alpha = 0.35f),
                        start = Offset(centerX, padTop),
                        end = Offset(centerX, padTop + chartH),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                    val x = padLeft + idx * groupW + (groupW - barW) / 2
                    val barH = (data.current[idx] / niceMaxVal) * chartH * animProg
                    val top = padTop + chartH - barH
                    drawRoundRect(
                        color = data.barColor,
                        topLeft = Offset(x, top),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(cornerR, cornerR),
                        style = Stroke(1.5.dp.toPx())
                    )
                }
            }
        }

        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@let
            if (idx !in 0 until n) return@let
            val padLeftPx = with(density) { padLeftDp.toPx() }
            val padRightPx = with(density) { padRightDp.toPx() }
            val cw = canvasSize.width - padLeftPx - padRightPx
            if (cw <= 0f) return@let

            val groupW = cw / n
            val pxX = padLeftPx + idx * groupW + groupW / 2
            val xDp = with(density) { pxX.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 160.dp
            val placeLeft = (xDp + 16.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 14.dp) else (xDp + 14.dp)
            val offsetY = 8.dp

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = tooltipTitle(idx),
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    Text(
                        text = tooltipBody(idx, data.current[idx]),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = data.barColor
                    )
                    data.previous?.let { prev ->
                        if (idx in prev.indices) {
                            Text(
                                text = tooltipPrevBody(idx, prev[idx]),
                                fontSize = 11.sp,
                                color = data.lineColor
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Smooth Line Chart (area fill)
// ─────────────────────────────────────────────

data class LineChartData(
    val labels: List<String>,
    val series: List<LineSeries>
)

data class LineSeries(
    val values: List<Float>,
    val color: Color,
    val fillAlpha: Float = 0.12f,
    val dashed: Boolean = false
)

@Composable
fun SmoothLineChart(
    data: LineChartData,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "line_anim"
    )
    LaunchedEffect(data) { trigger = false; trigger = true }

    val labelStyle = TextStyle(fontSize = 10.sp, color = ChartColors.TextSecondary)

    Canvas(modifier = modifier) {
        val padLeft   = 12.dp.toPx()
        val padRight  = 12.dp.toPx()
        val padTop    = 8.dp.toPx()
        val padBottom = 24.dp.toPx()
        val chartW = size.width - padLeft - padRight
        val chartH = size.height - padTop - padBottom

        val allVals = data.series.flatMap { it.values }
        val maxVal = allVals.maxOrNull() ?: 1f
        val niceMax = niceMax(maxVal)

        repeat(4) { i ->
            val y = padTop + chartH * (1f - (i + 1).toFloat() / 4)
            drawLine(ChartColors.GridLine, Offset(padLeft, y), Offset(padLeft + chartW, y), 0.5.dp.toPx())
        }

        val n = data.series.firstOrNull()?.values?.size ?: return@Canvas

        fun xOf(i: Int) = padLeft + i * (chartW / (n - 1).coerceAtLeast(1))
        fun yOf(v: Float) = padTop + chartH - (v / niceMax) * chartH

        data.series.forEach { series ->
            val pts = series.values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }
            val visCount = (pts.size * animProg).toInt().coerceAtLeast(2).coerceAtMost(pts.size)
            val visPts = pts.take(visCount)

            val linePath = smoothPath(visPts)

            if (series.fillAlpha > 0f) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(visPts.last().x, padTop + chartH)
                    lineTo(visPts.first().x, padTop + chartH)
                    close()
                }
                drawPath(fillPath, Brush.verticalGradient(
                    colors = listOf(series.color.copy(alpha = series.fillAlpha), series.color.copy(alpha = 0f)),
                    startY = padTop, endY = padTop + chartH
                ))
            }

            drawPath(
                linePath,
                color = series.color,
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = if (series.dashed) PathEffect.dashPathEffect(floatArrayOf(6f, 4f)) else null,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            if (visPts.isNotEmpty()) {
                val pt = visPts.last()
                drawCircle(ChartColors.Surface, 4.dp.toPx(), pt)
                drawCircle(series.color, 3.dp.toPx(), pt, style = Stroke(1.5.dp.toPx()))
            }
        }

        val step = (n / 6).coerceAtLeast(1)
        data.labels.forEachIndexed { i, lbl ->
            if (i % step == 0 || i == n - 1) {
                val m = textMeasurer.measure(lbl, labelStyle)
                drawText(m, topLeft = Offset(xOf(i) - m.size.width / 2f, padTop + chartH + 5.dp.toPx()))
            }
        }
    }
}

// ─────────────────────────────────────────────
// Interactive Line Chart — with hover tooltip
// ─────────────────────────────────────────────

@Composable
fun InteractiveLineChart(
    labels: List<String>,
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    yAxisFormat: (Float) -> String = { it.toInt().toString() },
    tooltipTitle: (Int) -> String = { labels.getOrElse(it) { "" } },
    tooltipBody: (Int, Float) -> String = { _, v -> v.toString() },
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "ilc_anim"
    )
    LaunchedEffect(values) { trigger = false; trigger = true }

    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    val padLeftDp   = 44.dp
    val padRightDp  = 16.dp
    val padTopDp    = 10.dp
    val padBottomDp = 26.dp

    val niceMaxVal = remember(values) { niceMax(values.maxOrNull() ?: 1f) }
    val n = values.size

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(values, canvasSize) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move,
                                PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val padLeft = with(density) { padLeftDp.toPx() }
                                    val padRight = with(density) { padRightDp.toPx() }
                                    val padTop = with(density) { padTopDp.toPx() }
                                    val padBottom = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - padLeft - padRight
                                    val ch = canvasSize.height - padTop - padBottom
                                    val inside = pos.x in (padLeft - 16f)..(padLeft + cw + 16f) &&
                                                 pos.y in (padTop - 8f)..(padTop + ch + 8f)
                                    if (cw > 0f && inside) {
                                        val xStep = cw / (n - 1).coerceAtLeast(1)
                                        val idx = ((pos.x - padLeft) / xStep + 0.5f).toInt()
                                            .coerceIn(0, n - 1)
                                        hoverIdx = idx
                                    } else {
                                        hoverIdx = null
                                    }
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val padLeft   = padLeftDp.toPx()
            val padRight  = padRightDp.toPx()
            val padTop    = padTopDp.toPx()
            val padBottom = padBottomDp.toPx()
            val cw = size.width - padLeft - padRight
            val ch = size.height - padTop - padBottom

            val axisStyle  = TextStyle(fontSize = 10.sp, color = ChartColors.TextSecondary)
            val labelStyle = TextStyle(fontSize = 10.sp, color = ChartColors.TextSecondary)

            // y-axis ticks + grid (5 lines: 0, 1/4, 2/4, 3/4, 4/4)
            repeat(5) { i ->
                val ratio = i.toFloat() / 4f
                val y = padTop + ch * (1f - ratio)
                drawLine(
                    color = ChartColors.GridLine,
                    start = Offset(padLeft, y),
                    end = Offset(padLeft + cw, y),
                    strokeWidth = 0.5.dp.toPx(),
                    pathEffect = if (i == 0) null else PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                )
                val v = niceMaxVal * ratio
                val l = textMeasurer.measure(yAxisFormat(v), axisStyle)
                drawText(l, topLeft = Offset(padLeft - l.size.width - 6.dp.toPx(), y - l.size.height / 2))
            }

            if (n == 0 || cw <= 0f || ch <= 0f) return@Canvas
            val xStep = cw / (n - 1).coerceAtLeast(1)
            fun xOf(i: Int) = padLeft + i * xStep
            fun yOf(v: Float) = padTop + ch - (v / niceMaxVal) * ch

            // line points
            val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }
            val visCount = (pts.size * animProg).toInt().coerceAtLeast(2).coerceAtMost(pts.size)
            val visPts = pts.take(visCount)
            val linePath = smoothPath(visPts)

            // area fill
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(visPts.last().x, padTop + ch)
                lineTo(visPts.first().x, padTop + ch)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = 0.22f), color.copy(alpha = 0f)),
                    startY = padTop, endY = padTop + ch
                )
            )

            // line
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // x labels
            labels.forEachIndexed { i, lbl ->
                val l = textMeasurer.measure(lbl, labelStyle)
                drawText(l, topLeft = Offset(xOf(i) - l.size.width / 2, padTop + ch + 6.dp.toPx()))
            }

            // hover guide line + emphasized dot
            hoverIdx?.let { idx ->
                if (idx in 0 until visCount) {
                    val pt = pts[idx]
                    drawLine(
                        color = color.copy(alpha = 0.35f),
                        start = Offset(pt.x, padTop),
                        end = Offset(pt.x, padTop + ch),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                    drawCircle(Color.White, 6.dp.toPx(), pt)
                    drawCircle(color, 6.dp.toPx(), pt, style = Stroke(2.dp.toPx()))
                }
            }
        }

        // Tooltip overlay
        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@let
            val padLeftPx = with(density) { padLeftDp.toPx() }
            val padRightPx = with(density) { padRightDp.toPx() }
            val padTopPx = with(density) { padTopDp.toPx() }
            val padBottomPx = with(density) { padBottomDp.toPx() }
            val cw = canvasSize.width - padLeftPx - padRightPx
            val ch = canvasSize.height - padTopPx - padBottomPx
            if (cw <= 0f || ch <= 0f) return@let

            val xStep = cw / (n - 1).coerceAtLeast(1)
            val pxX = padLeftPx + idx * xStep
            val pxY = padTopPx + ch - (values[idx] / niceMaxVal) * ch

            val xDp = with(density) { pxX.toDp() }
            val yDp = with(density) { pxY.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 160.dp
            val placeLeft = (xDp + 16.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 14.dp) else (xDp + 14.dp)
            val offsetY = (yDp - 22.dp).coerceAtLeast(4.dp)

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = tooltipTitle(idx),
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    Text(
                        text = tooltipBody(idx, values[idx]),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Grouped Bar Chart (game comparison)
// ─────────────────────────────────────────────

data class GroupedBarData(
    val labels: List<String>,
    val groups: List<BarGroup>
)

data class BarGroup(
    val name: String,
    val values: List<Float>,
    val color: Color
)

@Composable
fun GroupedBarChart(
    data: GroupedBarData,
    modifier: Modifier = Modifier,
    valueFormat: (BarGroup, Float) -> String = { _, v -> v.toString() },
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "grouped_anim"
    )
    LaunchedEffect(data) { trigger = false; trigger = true }

    val labelStyle = TextStyle(fontSize = 10.sp, color = ChartColors.TextSecondary)
    val axisStyle = TextStyle(fontSize = 9.sp, color = ChartColors.TextSecondary)

    val padLeftDp = 40.dp
    val padRightDp = 12.dp
    val padTopDp = 8.dp
    val padBottomDp = 36.dp

    val nCats = data.labels.size
    val niceMaxVal = remember(data) {
        niceMax(data.groups.flatMap { it.values }.maxOrNull() ?: 1f)
    }

    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(data, canvasSize) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move,
                                PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val padLeft = with(density) { padLeftDp.toPx() }
                                    val padRight = with(density) { padRightDp.toPx() }
                                    val padTop = with(density) { padTopDp.toPx() }
                                    val padBottom = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - padLeft - padRight
                                    val ch = canvasSize.height - padTop - padBottom
                                    val inside = pos.x in (padLeft - 8f)..(padLeft + cw + 8f) &&
                                                 pos.y in (padTop - 8f)..(padTop + ch + 8f)
                                    if (cw > 0f && inside && nCats > 0) {
                                        val groupW = cw / nCats
                                        val idx = ((pos.x - padLeft) / groupW).toInt().coerceIn(0, nCats - 1)
                                        hoverIdx = idx
                                    } else {
                                        hoverIdx = null
                                    }
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val padLeft   = padLeftDp.toPx()
            val padRight  = padRightDp.toPx()
            val padTop    = padTopDp.toPx()
            val padBottom = padBottomDp.toPx()
            val chartW = size.width - padLeft - padRight
            val chartH = size.height - padTop - padBottom

            val nGroups = data.groups.size
            if (nCats == 0 || nGroups == 0 || chartW <= 0f || chartH <= 0f) return@Canvas
            val groupW = chartW / nCats
            val barW = (groupW * 0.7f) / nGroups
            val gap = (groupW * 0.3f) / (nGroups + 1)
            val cornerR = 3.dp.toPx()

            repeat(4) { i ->
                val y = padTop + chartH * (1f - (i + 1) / 4f)
                drawLine(ChartColors.GridLine, Offset(padLeft, y), Offset(padLeft + chartW, y), 0.5.dp.toPx())
                val v = niceMaxVal * (i + 1) / 4
                val lbl = textMeasurer.measure(formatCompact(v), axisStyle)
                drawText(lbl, topLeft = Offset(padLeft - lbl.size.width - 5.dp.toPx(), y - lbl.size.height / 2))
            }

            hoverIdx?.let { idx ->
                if (idx in 0 until nCats) {
                    drawRect(
                        color = ChartColors.Gray400.copy(alpha = 0.08f),
                        topLeft = Offset(padLeft + idx * groupW, padTop),
                        size = Size(groupW, chartH)
                    )
                }
            }

            data.labels.forEachIndexed { catIdx, catLabel ->
                data.groups.forEachIndexed { gIdx, group ->
                    val value = group.values[catIdx]
                    val barH = (value / niceMaxVal) * chartH * animProg
                    val x = padLeft + catIdx * groupW + gap + gIdx * (barW + gap / nGroups)
                    val top = padTop + chartH - barH

                    drawRoundRect(
                        color = group.color.copy(alpha = 0.15f),
                        topLeft = Offset(x, padTop),
                        size = Size(barW, chartH),
                        cornerRadius = CornerRadius(cornerR)
                    )
                    drawRoundRect(
                        color = group.color,
                        topLeft = Offset(x, top),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(cornerR)
                    )
                }
                val lbl = textMeasurer.measure(catLabel, labelStyle)
                val centerX = padLeft + catIdx * groupW + groupW / 2
                drawText(lbl, topLeft = Offset(centerX - lbl.size.width / 2, padTop + chartH + 5.dp.toPx()))
            }
        }

        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@let
            if (idx !in 0 until nCats) return@let
            val padLeftPx = with(density) { padLeftDp.toPx() }
            val padRightPx = with(density) { padRightDp.toPx() }
            val cw = canvasSize.width - padLeftPx - padRightPx
            if (cw <= 0f) return@let

            val groupW = cw / nCats
            val pxX = padLeftPx + idx * groupW + groupW / 2
            val xDp = with(density) { pxX.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 180.dp
            val placeLeft = (xDp + 16.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 14.dp) else (xDp + 14.dp)
            val offsetY = 8.dp

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = data.labels[idx],
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    data.groups.forEach { group ->
                        val v = group.values.getOrElse(idx) { 0f }
                        Text(
                            text = "${group.name}: ${valueFormat(group, v)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = group.color
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Horizontal Bar (ranking / top games)
// ─────────────────────────────────────────────

data class HBarData(
    val items: List<HBarItem>
)

data class HBarItem(
    val label: String,
    val value: Float,
    val color: Color,
    val displayValue: String
)

@Composable
fun HorizontalBarChart(
    data: HBarData,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "hbar_anim"
    )
    LaunchedEffect(data) { trigger = false; trigger = true }

    val labelStyle = TextStyle(fontSize = 11.sp, color = ChartColors.TextPrimary, fontWeight = FontWeight.Normal)
    val valStyle = TextStyle(fontSize = 11.sp, color = ChartColors.TextSecondary)

    val rowH = 32.dp
    val totalH = rowH * data.items.size

    Canvas(modifier = modifier.height(totalH)) {
        val padLeft  = 120.dp.toPx()
        val padRight = 56.dp.toPx()
        val chartW = size.width - padLeft - padRight
        val maxVal = data.items.maxOf { it.value }

        data.items.forEachIndexed { i, item ->
            val y = i * rowH.toPx() + rowH.toPx() / 2
            val barH = 6.dp.toPx()
            val barW = (item.value / maxVal) * chartW * animProg

            val lbl = textMeasurer.measure(item.label, labelStyle)
            drawText(lbl, topLeft = Offset(0f, y - lbl.size.height / 2))

            drawRoundRect(
                color = item.color.copy(alpha = 0.12f),
                topLeft = Offset(padLeft, y - barH / 2),
                size = Size(chartW, barH),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
            if (barW > 0f) {
                drawRoundRect(
                    color = item.color,
                    topLeft = Offset(padLeft, y - barH / 2),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
            val valTxt = textMeasurer.measure(item.displayValue, valStyle)
            drawText(valTxt, topLeft = Offset(padLeft + chartW + 8.dp.toPx(), y - valTxt.size.height / 2))
        }
    }
}

// ─────────────────────────────────────────────
// Donut Chart
// ─────────────────────────────────────────────

data class DonutData(
    val slices: List<DonutSlice>
)

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    data: DonutData,
    centerLabel: String = "",
    centerSub: String = "",
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "donut_anim"
    )
    LaunchedEffect(data) { trigger = false; trigger = true }

    Canvas(modifier = modifier) {
        val total = data.slices.sumOf { it.value.toDouble() }.toFloat()
        val strokeW = size.minDimension * 0.18f
        val radius = (size.minDimension / 2) - strokeW / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        data.slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f * animProg
            drawArc(
                color = slice.color.copy(alpha = 0.1f),
                startAngle = startAngle,
                sweepAngle = (slice.value / total) * 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeW, cap = StrokeCap.Butt)
            )
            drawArc(
                color = slice.color,
                startAngle = startAngle + 0.8f,
                sweepAngle = (sweep - 1.6f).coerceAtLeast(0f),
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeW, cap = StrokeCap.Round)
            )
            startAngle += (slice.value / total) * 360f
        }

        if (centerLabel.isNotEmpty()) {
            val mainStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
            val subStyle = TextStyle(fontSize = 11.sp, color = ChartColors.TextSecondary)
            val main = textMeasurer.measure(centerLabel, mainStyle)
            val sub = textMeasurer.measure(centerSub, subStyle)
            drawText(main, topLeft = Offset(center.x - main.size.width / 2, center.y - main.size.height / 2 - sub.size.height / 2 - 2.dp.toPx()))
            drawText(sub, topLeft = Offset(center.x - sub.size.width / 2, center.y + main.size.height / 2 - sub.size.height / 2 + 2.dp.toPx()))
        }
    }
}

// ─────────────────────────────────────────────
// Heatmap Calendar
// ─────────────────────────────────────────────

data class HeatmapData(
    val weeks: List<List<Int>>,
    val maxVal: Int = 100
)

@Composable
fun HeatmapChart(
    data: HeatmapData,
    modifier: Modifier = Modifier,
    tooltipBody: (week: Int, day: Int, value: Int) -> String = { _, _, v -> "Lượt chơi: $v" },
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val dayLabels = listOf("T2","T3","T4","T5","T6","T7","CN")
    val labelStyle = TextStyle(fontSize = 9.sp, color = ChartColors.TextSecondary)

    val colors = listOf(
        Color(0xFFF1EFE8),
        Color(0xFFC0DD97),
        Color(0xFF97C459),
        Color(0xFF639922),
        Color(0xFF3B6D11)
    )

    val padLeftDp = 36.dp
    val padTopDp = 14.dp
    val nDays = 7

    var hoverCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    fun cellSizePx(widthPx: Float): Float {
        val padLeft = with(density) { padLeftDp.toPx() }
        val maxCell = with(density) { 36.dp.toPx() }
        return ((widthPx - padLeft) / nDays).coerceAtMost(maxCell)
    }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(data, canvasSize) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move,
                                PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val padLeft = with(density) { padLeftDp.toPx() }
                                    val padTop = with(density) { padTopDp.toPx() }
                                    val cs = cellSizePx(canvasSize.width)
                                    if (cs <= 0f) { hoverCell = null; continue }
                                    val d = ((pos.x - padLeft) / cs).toInt()
                                    val w = ((pos.y - padTop) / cs).toInt()
                                    if (w in data.weeks.indices &&
                                        d in 0 until nDays &&
                                        d in data.weeks[w].indices) {
                                        hoverCell = w to d
                                    } else {
                                        hoverCell = null
                                    }
                                }
                                PointerEventType.Exit -> hoverCell = null
                            }
                        }
                    }
                }
        ) {
            val padLeft = padLeftDp.toPx()
            val padTop  = padTopDp.toPx()
            val cellSize = ((size.width - padLeft) / nDays).coerceAtMost(36.dp.toPx())
            val gap = 3.dp.toPx()
            val effectiveCell = cellSize - gap

            dayLabels.forEachIndexed { d, lbl ->
                val m = textMeasurer.measure(lbl, labelStyle)
                drawText(m, topLeft = Offset(
                    padLeft + d * cellSize + effectiveCell / 2 - m.size.width / 2,
                    0f
                ))
            }

            data.weeks.forEachIndexed { w, week ->
                val wlbl = textMeasurer.measure("Tuần ${w + 1}", labelStyle)
                drawText(wlbl, topLeft = Offset(
                    0f,
                    padTop + w * cellSize + effectiveCell / 2 - wlbl.size.height / 2
                ))

                week.forEachIndexed { d, value ->
                    val ratio = (value.toFloat() / data.maxVal).coerceIn(0f, 1f)
                    val colorIdx = (ratio * (colors.size - 1)).toInt().coerceIn(0, colors.size - 1)
                    val x = padLeft + d * cellSize
                    val y = padTop + w * cellSize

                    drawRoundRect(
                        color = colors[colorIdx],
                        topLeft = Offset(x, y),
                        size = Size(effectiveCell, effectiveCell),
                        cornerRadius = CornerRadius(3.dp.toPx())
                    )
                }
            }

            hoverCell?.let { (w, d) ->
                val x = padLeft + d * cellSize
                val y = padTop + w * cellSize
                drawRoundRect(
                    color = ChartColors.TextPrimary,
                    topLeft = Offset(x, y),
                    size = Size(effectiveCell, effectiveCell),
                    cornerRadius = CornerRadius(3.dp.toPx()),
                    style = Stroke(1.5.dp.toPx())
                )
            }
        }

        hoverCell?.let { (w, d) ->
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@let
            val padLeftPx = with(density) { padLeftDp.toPx() }
            val padTopPx = with(density) { padTopDp.toPx() }
            val cs = cellSizePx(canvasSize.width)
            if (cs <= 0f) return@let

            val pxX = padLeftPx + d * cs + cs / 2
            val pxY = padTopPx + w * cs
            val xDp = with(density) { pxX.toDp() }
            val yDp = with(density) { pxY.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 160.dp
            val placeLeft = (xDp + 16.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 10.dp) else (xDp + 10.dp)
            val offsetY = (yDp - 30.dp).coerceAtLeast(4.dp)

            val value = data.weeks.getOrNull(w)?.getOrNull(d) ?: 0
            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = "Tuần ${w + 1} · ${dayLabels[d]}",
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    Text(
                        text = tooltipBody(w, d, value),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChartColors.TextPrimary
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Sparkline
// ─────────────────────────────────────────────

@Composable
fun Sparkline(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    showArea: Boolean = true
) {
    var trigger by remember { mutableStateOf(false) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(600, easing = EaseOutCubic),
        label = "spark_anim"
    )
    LaunchedEffect(values) { trigger = false; trigger = true }

    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val max = values.maxOrNull() ?: 1f
        val min = values.minOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(1f)
        val n = values.size

        fun xOf(i: Int) = i * (size.width / (n - 1))
        fun yOf(v: Float) = size.height - ((v - min) / range) * size.height * 0.85f - size.height * 0.07f

        val visCount = (n * animProg).toInt().coerceAtLeast(2).coerceAtMost(n)
        val pts = (0 until visCount).map { i -> Offset(xOf(i), yOf(values[i])) }
        val path = smoothPath(pts)

        if (showArea) {
            val fill = Path().apply {
                addPath(path)
                lineTo(pts.last().x, size.height)
                lineTo(pts.first().x, size.height)
                close()
            }
            drawPath(fill, Brush.verticalGradient(
                listOf(color.copy(alpha = 0.25f), color.copy(alpha = 0f)),
                startY = 0f, endY = size.height
            ))
        }

        drawPath(path, color = color, style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

        if (pts.isNotEmpty()) {
            drawCircle(Color.White, 3.dp.toPx(), pts.last())
            drawCircle(color, 2.dp.toPx(), pts.last(), style = Stroke(1.5.dp.toPx()))
        }
    }
}

// ─────────────────────────────────────────────
// Segment / Progress Bar
// ─────────────────────────────────────────────

data class SegmentData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun SegmentBar(
    segments: List<SegmentData>,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    tooltipTitle: ((Int, SegmentData) -> String)? = null,
    tooltipBody: ((Int, SegmentData, Float) -> String)? = null
) {
    val total = segments.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val density = LocalDensity.current
    var trigger by remember { mutableStateOf(false) }
    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val animProg by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "seg_anim"
    )
    LaunchedEffect(segments) { trigger = false; trigger = true }

    // Mở rộng hit area lên 22dp để dễ trỏ, dù visual bar vẫn `height` thật (10dp).
    val hitPadVertical = 6.dp
    Box(modifier = modifier.height(height + hitPadVertical * 2)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(segments, canvasSize, tooltipTitle, tooltipBody) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move,
                                PointerEventType.Enter -> {
                                    if (tooltipTitle == null && tooltipBody == null) continue
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    if (pos.x !in 0f..canvasSize.width || pos.y !in 0f..canvasSize.height) {
                                        hoverIdx = null
                                        continue
                                    }

                                    val gap = with(density) { 2.dp.toPx() }
                                    val n = segments.size
                                    var x = 0f
                                    var found: Int? = null
                                    segments.forEachIndexed { i, seg ->
                                        val w = ((seg.value / total) * canvasSize.width) -
                                            (if (i < n - 1) gap else 0f)
                                        if (found == null && w > 0f && pos.x in x..(x + w)) {
                                            found = i
                                        }
                                        x += w.coerceAtLeast(0f) + gap
                                    }
                                    hoverIdx = found
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            // Vẽ bar ở giữa box (chừa hitPadVertical trên dưới).
            val padPx = hitPadVertical.toPx()
            val barTop = padPx
            val barBottom = size.height - padPx
            val barHeight = barBottom - barTop
            val r = barHeight / 2f
            val gap = 2.dp.toPx()
            val n = segments.size
            var x = 0f

            // Compute target rect cho từng segment để dùng cho cả vẽ + highlight.
            data class SegRect(val left: Float, val right: Float, val isFirst: Boolean, val isLast: Boolean)
            val rects = mutableListOf<SegRect?>()
            segments.forEachIndexed { i, seg ->
                val w = ((seg.value / total) * size.width * animProg) - (if (i < n - 1) gap else 0f)
                if (w <= 0f) {
                    rects += null
                } else {
                    val rect = SegRect(x, x + w, i == 0, i == n - 1)
                    rects += rect

                    val path = Path().apply {
                        val tl = if (rect.isFirst) r else 0f
                        val tr = if (rect.isLast) r else 0f
                        addRoundRect(RoundRect(
                            left = rect.left, top = barTop, right = rect.right, bottom = barBottom,
                            topLeftCornerRadius = CornerRadius(tl),
                            topRightCornerRadius = CornerRadius(tr),
                            bottomRightCornerRadius = CornerRadius(tr),
                            bottomLeftCornerRadius = CornerRadius(tl)
                        ))
                    }
                    drawPath(path, seg.color)
                    x += w + gap
                }
            }

            // Highlight: vẽ stroke + lift trên segment đang hover.
            hoverIdx?.let { idx ->
                val rect = rects.getOrNull(idx) ?: return@let
                val seg = segments.getOrNull(idx) ?: return@let
                val lift = 2.dp.toPx()
                val tl = if (rect.isFirst) r else 0f
                val tr = if (rect.isLast) r else 0f
                val path = Path().apply {
                    addRoundRect(RoundRect(
                        left = rect.left,
                        top = barTop - lift,
                        right = rect.right,
                        bottom = barBottom + lift,
                        topLeftCornerRadius = CornerRadius(tl),
                        topRightCornerRadius = CornerRadius(tr),
                        bottomRightCornerRadius = CornerRadius(tr),
                        bottomLeftCornerRadius = CornerRadius(tl)
                    ))
                }
                drawPath(
                    path,
                    color = seg.color,
                    style = Stroke(1.5.dp.toPx())
                )
            }
        }

        hoverIdx?.let { idx ->
            val seg = segments.getOrNull(idx) ?: return@let
            val title = tooltipTitle?.invoke(idx, seg) ?: seg.label
            val body = tooltipBody?.invoke(idx, seg, seg.value / total * 100f)
            if (canvasSize.width <= 0f || title.isBlank() && body.isNullOrBlank()) return@let

            // Đặt tooltip ngay tại tâm segment đang hover, lệch lên trên bar — khớp UX
            // các chart khác (BarCompareChart, InteractiveBarChart).
            val gap = with(density) { 2.dp.toPx() }
            val n = segments.size
            var cumStart = 0f
            var segCenterPx = 0f
            segments.forEachIndexed { i, s ->
                val w = ((s.value / total) * canvasSize.width) - (if (i < n - 1) gap else 0f)
                if (i == idx && w > 0f) segCenterPx = cumStart + w / 2f
                cumStart += w.coerceAtLeast(0f) + gap
            }
            val xDp = with(density) { segCenterPx.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }
            val tooltipApproxW = 170.dp
            val placeLeft = (xDp + 10.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 8.dp) else (xDp + 8.dp)

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = 0.dp),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    if (!body.isNullOrBlank()) {
                        Text(
                            text = body,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = seg.color
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// Utilities
// ─────────────────────────────────────────────

private fun niceMax(value: Float): Float {
    if (value <= 0f) return 1f
    val magnitude = Math.pow(10.0, Math.floor(Math.log10(value.toDouble()))).toFloat()
    val normalized = value / magnitude
    val nice = when {
        normalized <= 1f -> 1f
        normalized <= 2f -> 2f
        normalized <= 5f -> 5f
        else -> 10f
    }
    return nice * magnitude
}

private fun formatCompact(v: Float): String {
    return when {
        v >= 1_000_000 -> "${(v / 1_000_000).toInt()}M"
        v >= 1_000 -> "${(v / 1_000).toInt()}k"
        else -> v.toInt().toString()
    }
}

private fun smoothPath(pts: List<Offset>): Path {
    val path = Path()
    if (pts.isEmpty()) return path
    path.moveTo(pts[0].x, pts[0].y)
    if (pts.size == 1) return path
    for (i in 1 until pts.size) {
        val prev = pts[i - 1]
        val curr = pts[i]
        val cx = (prev.x + curr.x) / 2
        path.cubicTo(cx, prev.y, cx, curr.y, curr.x, curr.y)
    }
    return path
}
