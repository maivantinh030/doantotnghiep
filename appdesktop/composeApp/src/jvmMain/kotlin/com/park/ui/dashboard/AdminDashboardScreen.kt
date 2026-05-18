package com.park.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.park.ui.charts.*
import com.park.ui.common.DATE_FMT
import com.park.ui.common.countAxisLabel
import com.park.ui.common.formatCountVi
import com.park.ui.common.formatMoneyVi
import com.park.ui.common.moneyAxisLabel
import com.park.ui.common.pickMoneyScale
import com.park.ui.component.ErrorBanner
import com.park.viewmodel.AdminOverviewViewModel
import kotlin.math.roundToInt

// ─────────────────────────────────────────────
// Dashboard Screen
// ─────────────────────────────────────────────

@Composable
fun AdminDashboardScreen(viewModel: AdminOverviewViewModel) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var compareMetric by remember { mutableStateOf("Doanh thu") }
    val scroll = rememberScrollState()

    val overviewValues = when (compareMetric) {
        "Lượt chơi" -> ui.overviewPlays
        "Người dùng" -> ui.overviewPlayers
        else -> ui.overviewRevenue
    }
    val overviewColor = when (compareMetric) {
        "Lượt chơi" -> ChartColors.Teal400
        "Người dùng" -> ChartColors.Amber400
        else -> ChartColors.Green400
    }

    Surface(color = Color(0xFFF7F6F3), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Phân tích & thống kê", fontSize = 20.sp,
                        fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
                    Text("Công Viên Giải Trí · ${ui.today.format(DATE_FMT)}",
                        fontSize = 12.sp, color = ChartColors.TextSecondary)
                }
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0x1A888780))
                ) {
                    Text(
                        "Tháng ${"%02d".format(ui.today.monthValue)}/${ui.today.year}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChartColors.TextPrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            if (ui.errorMessage != null) {
                ErrorBanner(ui.errorMessage!!, onRetry = { viewModel.refresh() })
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KpiCard(
                    label = "Doanh thu",
                    value = ui.kRevenue,
                    change = ui.revenueChange,
                    up = ui.revenueUp,
                    sparkValues = ui.mtdRevenue,
                    sparkColor = ChartColors.Blue400,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    label = "Lượt chơi",
                    value = ui.kPlays,
                    change = ui.playsChange,
                    up = ui.playsUp,
                    sparkValues = ui.mtdPlays,
                    sparkColor = ChartColors.Teal400,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    label = "Người dùng mới",
                    value = ui.kNewUsers,
                    change = ui.newUsersChange,
                    up = ui.newUsersUp,
                    sparkValues = ui.mtdNewUsers,
                    sparkColor = if (ui.newUsersUp) ChartColors.Teal400 else ChartColors.Red400,
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    label = "Doanh thu / lượt",
                    value = ui.kArpu,
                    change = ui.arpuChange,
                    up = ui.arpuUp,
                    sparkValues = ui.mtdRevenue.zip(ui.mtdPlays).map { (r, p) -> if (p > 0) r / p else 0f },
                    sparkColor = ChartColors.Amber400,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashCard(modifier = Modifier.weight(3f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            CardTitle("Overview")
                            Text("Hiệu suất theo tháng — năm 2026",
                                fontSize = 11.sp, color = ChartColors.TextSecondary)
                        }
                        MetricToggle(
                            options = listOf("Doanh thu","Lượt chơi","Người dùng"),
                            selected = compareMetric,
                            onSelect = { compareMetric = it }
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    val yFmt: (Float) -> String = when (compareMetric) {
                        "Lượt chơi"  -> { v -> countAxisLabel(v) }
                        "Người dùng" -> { v -> countAxisLabel(v) }
                        else         -> { v -> moneyAxisLabel(v) }
                    }
                    val bodyFmt: (Int, Float) -> String = when (compareMetric) {
                        "Lượt chơi"  -> { _, v -> "Lượt chơi: ${formatCountVi(v.toLong())}" }
                        "Người dùng" -> { _, v -> "Người dùng mới: ${formatCountVi(v.toLong())}" }
                        else         -> { _, v -> "Doanh thu: ${formatMoneyVi(v)}" }
                    }

                    InteractiveLineChart(
                        labels = ui.overviewLabels,
                        values = overviewValues,
                        color = overviewColor,
                        yAxisFormat = yFmt,
                        tooltipTitle = { idx -> ui.overviewLabels.getOrElse(idx) { "T${idx + 1}" } },
                        tooltipBody = bodyFmt,
                        modifier = Modifier.fillMaxWidth().height(260.dp)
                    )
                }

                DashCard(modifier = Modifier.weight(2f)) {
                    CardTitle("Kênh nhận thẻ")
                    Text("Tỷ trọng người đăng ký thẻ qua app vs tại quầy",
                        fontSize = 11.sp, color = ChartColors.TextSecondary)
                    Spacer(Modifier.height(10.dp))

                    val total = ui.withApp + ui.noApp
                    if (total == 0) {
                        Spacer(Modifier.height(40.dp))
                        Text(
                            "Chưa có thẻ nào được phát trong kỳ",
                            fontSize = 12.sp,
                            color = ChartColors.TextSecondary,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        )
                    } else {
                        val appPct = (ui.withApp.toFloat() / total * 100).toInt()
                        DonutChart(
                            data = DonutData(listOf(
                                DonutSlice("Qua app", ui.withApp.toFloat(), ChartColors.Blue400),
                                DonutSlice("Tại quầy", ui.noApp.toFloat(), ChartColors.Amber400),
                            )),
                            centerLabel = "$total",
                            centerSub = "thẻ",
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                        Spacer(Modifier.height(14.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChannelLegendRow(ChartColors.Blue400, "Qua app", ui.withApp, appPct)
                            ChannelLegendRow(ChartColors.Amber400, "Tại quầy", ui.noApp, 100 - appPct)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DashCard(modifier = Modifier.weight(3f)) {
                    CardTitle("Hiệu suất trò chơi")
                    Spacer(Modifier.height(4.dp))
                    val revScale = pickMoneyScale(ui.gameRevenue.maxOrNull() ?: 0f)
                    val playsMax = ui.gamePlays.maxOrNull() ?: 0f
                    val playsDivisor = when {
                        playsMax >= 1_000_000f -> 1_000_000f
                        playsMax >= 1_000f     -> 1_000f
                        else                    -> 1f
                    }
                    val playsUnit = when (playsDivisor) {
                        1_000_000f -> "M"
                        1_000f     -> "k"
                        else        -> ""
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        LegendItem(ChartColors.Blue400, "Doanh thu (${revScale.unitLabel})")
                        LegendItem(
                            ChartColors.Amber400,
                            if (playsUnit.isEmpty()) "Lượt chơi" else "Lượt chơi ($playsUnit)"
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    val revScaled = ui.gameRevenue.map { it / revScale.divisor }
                    val playsScaled = ui.gamePlays.map { it / playsDivisor }
                    GroupedBarChart(
                        data = GroupedBarData(
                            labels = ui.gameLabels,
                            groups = listOf(
                                BarGroup("Doanh thu", revScaled, ChartColors.Blue400),
                                BarGroup("Lượt chơi", playsScaled, ChartColors.Amber400)
                            )
                        ),
                        valueFormat = { group, v ->
                            if (group.name.startsWith("Doanh thu")) formatMoneyVi(v * revScale.divisor)
                            else formatCountVi((v * playsDivisor).toLong())
                        },
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                }

                DashCard(modifier = Modifier.weight(2f)) {
                    CardTitle("Tình trạng thẻ")
                    Spacer(Modifier.height(10.dp))
                    val totalCards = ui.cardActive + ui.cardBlocked + ui.cardAvailable
                    fun statusPct(value: Int): Int =
                        if (totalCards <= 0) 0 else (value * 100f / totalCards).roundToInt()
                    val activePct = statusPct(ui.cardActive)
                    val blockedPct = statusPct(ui.cardBlocked)
                    val availablePct = if (totalCards <= 0) 0
                    else (100 - activePct - blockedPct).coerceIn(0, 100)
                    val statusSegments = if (totalCards <= 0) {
                        listOf(SegmentData("Chưa có dữ liệu", 1f, ChartColors.Gray100))
                    } else {
                        listOf(
                            SegmentData("Đang dùng", ui.cardActive.toFloat(), ChartColors.Blue400),
                            SegmentData("Bị khóa", ui.cardBlocked.toFloat(), ChartColors.Red400),
                            SegmentData("Có sẵn", ui.cardAvailable.toFloat(), ChartColors.Gray100)
                        ).filter { it.value > 0f }
                    }
                    SegmentBar(
                        segments = statusSegments,
                        modifier = Modifier.fillMaxWidth(),
                        height = 10.dp,
                        tooltipTitle = { _, seg -> seg.label },
                        tooltipBody = { _, seg, pct ->
                            if (totalCards <= 0) "Chưa có thẻ"
                            else "${formatCountVi(seg.value)} thẻ · ${pct.roundToInt()}%"
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        LegendItem(ChartColors.Blue400, "Đang dùng $activePct%")
                        LegendItem(ChartColors.Red400, "Bị khóa $blockedPct%")
                        LegendItem(ChartColors.Gray100, "Có sẵn $availablePct%")
                    }
                    Spacer(Modifier.height(16.dp))
                    val lifecycleMax = maxOf(
                        ui.cardIssuedThisMonth,
                        ui.cardBlockedThisMonth,
                        ui.cardPendingRequests,
                        1
                    ).toFloat()
                    CardMetricRow("Cấp mới / tháng", formatCountVi(ui.cardIssuedThisMonth.toFloat()), "", null, lifecycleMax)
                    CardMetricRow("Bị khóa / tháng", formatCountVi(ui.cardBlockedThisMonth.toFloat()), "", null, lifecycleMax,
                        barColor = ChartColors.Red400)
                    CardMetricRow("Pending request", formatCountVi(ui.cardPendingRequests.toFloat()), "", null, lifecycleMax,
                        barColor = ChartColors.Amber400)
                }
            }

            DashCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    CardTitle("Lượt chơi theo giờ")
                    Text("TB tháng", fontSize = 11.sp, color = ChartColors.TextSecondary)
                }
                Spacer(Modifier.height(8.dp))
                val peakAvg = ui.hourPlays.maxOrNull()?.toInt() ?: 0
                Text(
                    "⏰ Cao điểm: ${ui.peakHourLabel}  ·  ${peakAvg} lượt/giờ",
                    fontSize = 11.sp, color = ChartColors.Amber400
                )
                Spacer(Modifier.height(8.dp))
                BarCompareChart(
                    data = BarChartData(
                        labels = ui.hourLabels,
                        current = ui.hourPlays,
                        barColor = ChartColors.Blue400
                    ) { "${it.toInt()}" },
                    tooltipTitle = { idx -> ui.hourLabels.getOrElse(idx) { "" } },
                    tooltipBody  = { _, v -> "Lượt chơi: ${formatCountVi(v)}" },
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────
// Sub-components
// ─────────────────────────────────────────────

@Composable
fun KpiCard(
    label: String,
    value: String,
    change: String,
    up: Boolean,
    sparkValues: List<Float>,
    sparkColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF1EFE8),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = ChartColors.TextSecondary,
                letterSpacing = 0.04.sp)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(value, fontSize = 26.sp, fontWeight = FontWeight.Medium,
                    color = ChartColors.TextPrimary, lineHeight = 28.sp)
                Sparkline(
                    values = sparkValues,
                    color = sparkColor,
                    showArea = true,
                    modifier = Modifier.width(64.dp).height(32.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            if (change.isNotEmpty()) {
                val changeColor = when {
                    up -> Color(0xFF3B6D11)
                    change.startsWith("±") -> ChartColors.TextSecondary
                    else -> Color(0xFFA32D2D)
                }
                Text(
                    text = "$change vs kỳ trước",
                    fontSize = 11.sp,
                    color = changeColor
                )
            } else {
                Text("", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun DashCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0x1A888780))
    ) {
        Column(modifier = Modifier.padding(18.dp), content = content)
    }
}

@Composable
fun CardTitle(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
}

@Suppress("unused") // hiện chưa dùng — giữ để mở rộng sau (đa phạm vi tháng/quý/năm)
@Composable
fun PeriodToggle(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Surface(
        color = Color(0xFFF1EFE8),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            options.forEach { opt ->
                val isOn = opt == selected
                Surface(
                    color = if (isOn) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                    border = if (isOn) BorderStroke(0.5.dp, Color(0x1A888780)) else null,
                    modifier = Modifier.clickable { onSelect(opt) }
                ) {
                    Text(opt, fontSize = 12.sp,
                        fontWeight = if (isOn) FontWeight.Medium else FontWeight.Normal,
                        color = if (isOn) ChartColors.TextPrimary else ChartColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp))
                }
            }
        }
    }
}

@Composable
fun MetricToggle(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Surface(
        color = Color(0xFFF1EFE8),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(modifier = Modifier.padding(2.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            options.forEach { opt ->
                val isOn = opt == selected
                Surface(
                    color = if (isOn) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(5.dp),
                    border = if (isOn) BorderStroke(0.5.dp, Color(0x1A888780)) else null,
                    modifier = Modifier.clickable { onSelect(opt) }
                ) {
                    Text(opt, fontSize = 11.sp,
                        fontWeight = if (isOn) FontWeight.Medium else FontWeight.Normal,
                        color = if (isOn) ChartColors.TextPrimary else ChartColors.TextSecondary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text(label, fontSize = 11.sp, color = ChartColors.TextSecondary)
    }
}

@Composable
fun LegendItemDashed(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Canvas(Modifier.size(14.dp, 8.dp)) {
            drawLine(color, Offset(0f, size.height/2), Offset(size.width, size.height/2),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
        }
        Text(label, fontSize = 11.sp, color = ChartColors.TextSecondary)
    }
}

@Composable
fun CardMetricRow(
    label: String,
    value: String,
    change: String,
    up: Boolean?,
    maxBarVal: Float,
    barColor: Color = ChartColors.Blue400
) {
    val changeColor = when (up) {
        true -> Color(0xFF3B6D11)
        false -> Color(0xFFA32D2D)
        null -> ChartColors.TextSecondary
    }
    Column(modifier = Modifier.padding(vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = ChartColors.TextSecondary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(change, fontSize = 11.sp, color = changeColor)
                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = ChartColors.TextPrimary)
            }
        }
        Spacer(Modifier.height(4.dp))
        val pct = (value.replace(".", "").replace(",", "").toFloatOrNull() ?: 0f) / maxBarVal
        Box(
            Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp))
                .background(barColor.copy(alpha = 0.12f))
        ) {
            Box(Modifier.fillMaxWidth(pct.coerceIn(0f, 1f)).fillMaxHeight()
                .clip(RoundedCornerShape(2.dp)).background(barColor))
        }
    }
}

@Composable
fun ChannelLegendRow(color: Color, label: String, count: Int, pct: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Text(label, fontSize = 12.sp, color = ChartColors.TextPrimary)
        Spacer(Modifier.weight(1f))
        Text("$count người", fontSize = 12.sp,
            fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
        Text("$pct%", fontSize = 11.sp, color = ChartColors.TextSecondary,
            modifier = Modifier.padding(start = 4.dp))
    }
}

