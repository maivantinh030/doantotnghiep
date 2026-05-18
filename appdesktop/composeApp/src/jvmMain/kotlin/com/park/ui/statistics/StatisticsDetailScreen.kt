package com.park.ui.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.park.data.model.GameDetailDTO
import com.park.ui.charts.ChartColors
import com.park.ui.common.DATE_FMT
import com.park.ui.common.TimeRange
import com.park.ui.common.formatMoneyVi
import com.park.ui.common.pickMoneyScale
import com.park.ui.component.ErrorBanner
import com.park.ui.component.LoadingOverlay
import com.park.viewmodel.GameLite
import com.park.viewmodel.StatisticsDetailViewModel
import java.time.LocalDate
import java.time.ZoneOffset

// ─────────────────────────────────────────────────────────────────────────────
// Label hằng số dùng cho chart DOW/Hourly
// ─────────────────────────────────────────────────────────────────────────────

private val DOW   = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
private val HOURS = listOf("6h","7h","8h","9h","10h","11h","12h","13h",
                            "14h","15h","16h","17h","18h","19h","20h","21h")

// ─────────────────────────────────────────────────────────────────────────────
// (Mock data đã được chuyển sang ViewModel + backend, helpers cũ đã xoá.)
// ─────────────────────────────────────────────────────────────────────────────

// ─────────────────────────────────────────────────────────────────────────────
// Root screen — split layout: sidebar + detail
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun StatisticsDetailScreen(viewModel: StatisticsDetailViewModel) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    Row(Modifier.fillMaxSize().background(Color(0xFFF7F6F3))) {

        Column(
            Modifier
                .width(240.dp).fillMaxHeight()
                .background(Color.White)
                .border(BorderStroke(0.5.dp, Color(0x1A888780)))
                .verticalScroll(rememberScrollState())
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("Thống kê chi tiết", fontSize = 13.sp,
                fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp))

            SidebarItem(
                label = "Tổng quan phân bố",
                sublabel = "So sánh & xếp hạng",
                color = ChartColors.Blue400,
                selected = ui.selectedGameId == null,
                onClick = { viewModel.selectGame(null) }
            )

            HorizontalDivider(color = Color(0x1A888780), modifier = Modifier.padding(vertical = 8.dp))

            var query by remember { mutableStateOf("") }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = {
                    Text("Tìm trò chơi...", fontSize = 12.sp,
                        color = ChartColors.TextSecondary)
                },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp,
                    color = ChartColors.TextPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChartColors.Blue400,
                    unfocusedBorderColor = Color(0x33888780)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            )

            val q = query.trim()
            val filteredGames = if (q.isEmpty()) ui.games
            else ui.games.filter { it.name.contains(q, ignoreCase = true) }

            if (filteredGames.isEmpty()) {
                Text("Không tìm thấy trò chơi", fontSize = 11.sp,
                    color = ChartColors.TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp))
            } else {
                filteredGames.groupBy { it.category }.forEach { (cat, list) ->
                    Text(cat.uppercase(), fontSize = 9.sp, color = ChartColors.TextSecondary,
                        letterSpacing = 0.07.sp,
                        modifier = Modifier.padding(start = 2.dp, top = 2.dp, bottom = 2.dp))
                    list.forEach { g ->
                        val playsLast30 = ui.gameDetails[g.id]?.playsFor(ui.today, TimeRange.Last30Days)?.toInt() ?: 0
                        SidebarItem(
                            label = g.name,
                            sublabel = "$playsLast30 lượt · ${g.ticketPriceK}k/lượt",
                            color = g.color,
                            selected = ui.selectedGameId == g.id,
                            onClick = { viewModel.selectGame(g.id) }
                        )
                    }
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (ui.errorMessage != null && ui.games.isEmpty()) {
                    ErrorBanner(ui.errorMessage!!, onRetry = { viewModel.refresh() })
                } else if (ui.selectedGameId == null) {
                    OverviewPanel(
                        games = ui.games,
                        details = ui.gameDetails,
                        range = ui.range,
                        today = ui.today,
                        onRangeChange = viewModel::setRange
                    )
                } else {
                    val game = ui.games.firstOrNull { it.id == ui.selectedGameId }
                    val dto = ui.gameDetails[ui.selectedGameId]
                    when {
                        game == null -> ErrorBanner("Trò chơi không tồn tại")
                        dto == null && ui.isLoading -> Text("Đang tải...", fontSize = 12.sp)
                        dto == null -> ErrorBanner(
                            "Không tải được dữ liệu trò chơi này",
                            onRetry = { viewModel.refresh() }
                        )
                        else -> GameDetailPanel(
                            game = game,
                            dto = dto,
                            allGames = ui.games,
                            allDetails = ui.gameDetails,
                            range = ui.range,
                            today = ui.today,
                            onRangeChange = viewModel::setRange
                        )
                    }
                }
            }
            LoadingOverlay(visible = ui.isLoading && ui.games.isEmpty())
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Overview panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun OverviewPanel(
    games: List<GameLite>,
    details: Map<String, GameDetailDTO>,
    range: TimeRange,
    today: LocalDate,
    onRangeChange: (TimeRange) -> Unit
) {
    val gamesWithRev = games.map { it to (details[it.id]?.revenueFor(today, range) ?: 0f) }
    val sorted = gamesWithRev.sortedByDescending { it.second }
    val totalRev = gamesWithRev.sumOf { it.second.toDouble() }.toFloat()
    val totalPlays = games.sumOf { (details[it.id]?.playsFor(today, range) ?: 0f).toDouble() }.toFloat()
    val avgRev = if (games.isNotEmpty()) totalRev / games.size else 0f
    val topGame = sorted.firstOrNull()
    val bottomGame = sorted.lastOrNull()
    var revAscending by remember { mutableStateOf(false) }
    var playsAscending by remember { mutableStateOf(false) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tổng quan & so sánh trò chơi", fontSize = 18.sp,
                fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
            Text("${range.label} · ${games.size} trò chơi",
                fontSize = 12.sp, color = ChartColors.TextSecondary)
        }
        TimeRangeSelector(range, onRangeChange)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniKpi(
            "Tổng doanh thu", formatMoneyVi(totalRev),
            range.label, null, Modifier.weight(1f)
        )
        MiniKpi("Tổng lượt chơi", "%,d".format(totalPlays.toInt()),
            range.label, null, Modifier.weight(1f))
        MiniKpi("TB / trò chơi", formatMoneyVi(avgRev),
            "${games.size} game", null, Modifier.weight(1f))
        if (topGame != null) {
            HighlightKpi(
                "Doanh thu cao nhất", topGame.first.name,
                formatMoneyVi(topGame.second), topGame.first.color, Modifier.weight(1f)
            )
        } else {
            MiniKpi("Doanh thu cao nhất", "—", "", null, Modifier.weight(1f))
        }
        if (bottomGame != null) {
            HighlightKpi(
                "Doanh thu thấp nhất", bottomGame.first.name,
                formatMoneyVi(bottomGame.second), bottomGame.first.color, Modifier.weight(1f)
            )
        } else {
            MiniKpi("Doanh thu thấp nhất", "—", "", null, Modifier.weight(1f))
        }
    }

    RankingCard(
        title = "Xếp hạng doanh thu — ${range.label}",
        unitLabel = "doanh thu",
        ascending = revAscending,
        onToggle = { revAscending = !revAscending },
    ) {
        RankingTable(
            games = games,
            ascending = revAscending,
            limit = 7,
            valueOf = { g -> details[g.id]?.revenueFor(today, range) ?: 0f },
            formatValue = { formatMoneyVi(it) }
        )
    }

    RankingCard(
        title = "Xếp hạng lượt chơi — ${range.label}",
        unitLabel = "lượt chơi",
        ascending = playsAscending,
        onToggle = { playsAscending = !playsAscending },
    ) {
        RankingTable(
            games = games,
            ascending = playsAscending,
            limit = 7,
            valueOf = { g -> details[g.id]?.playsFor(today, range) ?: 0f },
            formatValue = { "%,d lượt".format(it.toInt()) }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Single game detail panel
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun GameDetailPanel(
    game: GameLite,
    dto: GameDetailDTO,
    allGames: List<GameLite>,
    allDetails: Map<String, GameDetailDTO>,
    range: TimeRange,
    today: LocalDate,
    onRangeChange: (TimeRange) -> Unit
) {
    val colorByGameId = remember(allGames) { allGames.associate { it.id to it.color } }
    val revInRange = dto.revenueFor(today, range)
    val playsInRange = dto.playsFor(today, range)
    val totalRev = allDetails.values.sumOf { it.revenueFor(today, range).toDouble() }.toFloat()

    val revSeries = dto.revenueSeries(today, range)
    val playsSeries = dto.playsSeries(today, range)
    val parkAvgSeries = remember(range, allDetails) {
        val n = revSeries.values.size
        if (n == 0) emptyList()
        else (0 until n).map { i ->
            val list = allDetails.values.map { it.revenueSeries(today, range).values.getOrElse(i) { 0f } }
            if (list.isEmpty()) 0f else list.average().toFloat()
        }
    }
    val avgPerDay = revInRange / range.days.coerceAtLeast(1)

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(14.dp).clip(RoundedCornerShape(3.dp)).background(game.color))
            Column {
                Text(game.name, fontSize = 18.sp, fontWeight = FontWeight.Medium,
                    color = ChartColors.TextPrimary)
                Text("${dto.category} · ${game.ticketPriceK}k / lượt",
                    fontSize = 12.sp, color = ChartColors.TextSecondary)
            }
        }
        TimeRangeSelector(range, onRangeChange)
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MiniKpi("Doanh thu (${range.label})", formatMoneyVi(revInRange),
            range.label, null, Modifier.weight(1f))
        MiniKpi("Lượt chơi", "%,d".format(playsInRange.toInt()),
            range.label, null, Modifier.weight(1f))
        MiniKpi("TB / ngày", formatMoneyVi(avgPerDay),
            "${range.days} ngày", null, Modifier.weight(1f))
        MiniKpi("Giờ cao điểm", dto.peakHour,
            "trong ngày", null, Modifier.weight(1f))
    }

    // Tự chọn đơn vị (tỷ / triệu / nghìn / đ) theo magnitude max để chart không bị tụt dưới 0.
    val revTrendScale = pickMoneyScale(revSeries.values.maxOrNull() ?: 0f)
    val revCompareMax = maxOf(
        revSeries.values.maxOrNull() ?: 0f,
        parkAvgSeries.maxOrNull() ?: 0f
    )
    val revCompareScale = pickMoneyScale(revCompareMax)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DetailCard("Doanh thu (${revTrendScale.unitLabel}) — ${range.label}", Modifier.weight(1f)) {
            Spacer(Modifier.height(6.dp))
            if (revSeries.values.isEmpty()) {
                Text("Không đủ dữ liệu trong kỳ này", fontSize = 11.sp, color = ChartColors.TextSecondary)
            } else {
                TrendLineChart(
                    values = revSeries.values.map { it / revTrendScale.divisor },
                    labels = compactLabels(revSeries.labels),
                    color = game.color,
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    tooltipTitle = { idx -> revSeries.labels.getOrElse(idx) { "" } },
                    tooltipBody = { idx, _ ->
                        "Doanh thu: ${formatMoneyVi(revSeries.values.getOrElse(idx) { 0f })}"
                    }
                )
            }
        }
        DetailCard("Lượt chơi — ${range.label}", Modifier.weight(1f)) {
            Spacer(Modifier.height(6.dp))
            if (playsSeries.values.isEmpty()) {
                Text("Không đủ dữ liệu trong kỳ này", fontSize = 11.sp, color = ChartColors.TextSecondary)
            } else {
                TrendLineChart(
                    values = playsSeries.values,
                    labels = compactLabels(playsSeries.labels),
                    color = game.color.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    tooltipTitle = { idx -> playsSeries.labels.getOrElse(idx) { "" } },
                    tooltipBody = { _, v -> "Lượt chơi: ${"%,d".format(v.toInt())}" }
                )
            }
        }
    }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        DetailCard("Doanh thu (${revCompareScale.unitLabel}) — so với TB công viên (${range.label})", Modifier.weight(3f)) {
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(game.color))
                    Text(game.name, fontSize = 11.sp, color = ChartColors.TextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Canvas(Modifier.size(16.dp, 8.dp)) {
                        drawLine(ChartColors.Gray400,
                            Offset(0f, size.height / 2), Offset(size.width, size.height / 2),
                            1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 3f)))
                    }
                    Text("TB công viên", fontSize = 11.sp, color = ChartColors.TextSecondary)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (revSeries.values.isEmpty()) {
                Text("Không đủ dữ liệu trong kỳ này", fontSize = 11.sp, color = ChartColors.TextSecondary)
            } else {
                CompareLineChart(
                    primary = revSeries.values.map { it / revCompareScale.divisor },
                    comparison = parkAvgSeries.map { it / revCompareScale.divisor },
                    labels = revSeries.labels,
                    primaryColor = game.color,
                    primaryName = game.name,
                    comparisonName = "TB công viên",
                    tooltipTitle = { idx -> revSeries.labels.getOrElse(idx) { "" } },
                    // Hiển thị tooltip ở đơn vị raw (đồng) cho cả 2 series — tự switch tỷ/tr/k/đ.
                    tooltipFormat = { v -> formatMoneyVi(v * revCompareScale.divisor) },
                    modifier = Modifier.fillMaxWidth().height(170.dp)
                )
            }
        }
        DetailCard("Lượt chơi theo thứ", Modifier.weight(2f)) {
            Spacer(Modifier.height(6.dp))
            DowBarChart(dto.playsByDow.map { it.toFloat() }, DOW, game.color, Modifier.fillMaxWidth().height(150.dp))
        }
    }

    DetailCard("Phân bố lượt chơi theo giờ trong ngày", Modifier.fillMaxWidth()) {
        if (dto.playsByHour.isEmpty()) {
            Text("Không có dữ liệu", fontSize = 11.sp, color = ChartColors.TextSecondary)
        } else {
            val peakIdx = dto.playsByHour.indexOf(dto.playsByHour.max())
            val peakLabel = HOURS.getOrElse(peakIdx) { "—" }
            Text("Giờ cao điểm: $peakLabel (${dto.playsByHour[peakIdx]} lượt TB)",
                fontSize = 11.sp, color = game.color)
            Spacer(Modifier.height(8.dp))
            HourBarChart(dto.playsByHour.map { it.toFloat() }, HOURS, game.color, Modifier.fillMaxWidth().height(160.dp))
        }
    }

    DetailCard("Tỷ trọng trong tổng doanh thu công viên (${range.label})", Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        allDetails
            .map { (id, d) -> Triple(id, d, d.revenueFor(today, range)) }
            .sortedByDescending { it.third }
            .forEach { (id, d, rev) ->
                val isThis = id == game.id
                // Lấy màu thực của game từ allGames; fallback gray khi không khớp (lạ).
                val baseColor = colorByGameId[id] ?: Color(0xFFCCCCCC)
                val barFillColor = if (isThis) baseColor else baseColor.copy(alpha = 0.25f)
                val pct = if (totalRev > 0f) rev / totalRev else 0f
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.width(170.dp)) {
                        Box(Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(baseColor))
                        Text(d.name, fontSize = 12.sp,
                            color = if (isThis) ChartColors.TextPrimary else ChartColors.TextSecondary,
                            fontWeight = if (isThis) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1)
                    }
                    Box(Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                        .background(baseColor.copy(alpha = 0.15f))) {
                        Box(Modifier.fillMaxWidth(pct.coerceIn(0f, 1f)).fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(barFillColor))
                    }
                    Text("%.1f%%".format(pct * 100), fontSize = 12.sp,
                        color = if (isThis) baseColor else ChartColors.TextPrimary,
                        fontWeight = if (isThis) FontWeight.Medium else FontWeight.Normal,
                        modifier = Modifier.width(48.dp))
                    Text(formatMoneyVi(rev), fontSize = 11.sp,
                        color = ChartColors.TextSecondary, modifier = Modifier.width(56.dp))
                }
            }
    }
}

/** Giảm số lượng label trên trục x: chỉ giữ ~6 label cho dễ nhìn. */
private fun compactLabels(labels: List<String>): List<String> {
    val n = labels.size
    if (n <= 8) return labels
    val step = (n / 6).coerceAtLeast(1)
    return labels.mapIndexed { i, lbl ->
        if (i == 0 || i == n - 1 || i % step == 0) lbl else ""
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Charts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RankingCard(
    title: String,
    unitLabel: String,
    ascending: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0x1A888780))
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        title,
                        fontSize = 13.sp, fontWeight = FontWeight.Medium,
                        color = ChartColors.TextPrimary
                    )
                    val direction = if (ascending) "Tăng dần · 7 trò chơi thấp nhất"
                                    else           "Giảm dần · 7 trò chơi cao nhất"
                    Text("$direction · thanh ngang tỉ lệ với $unitLabel trong kỳ",
                        fontSize = 11.sp, color = ChartColors.TextSecondary)
                }
                SortDirectionToggle(ascending = ascending, onToggle = onToggle)
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun RankingTable(
    games: List<GameLite>,
    ascending: Boolean = false,
    limit: Int = Int.MAX_VALUE,
    valueOf: (GameLite) -> Float,
    formatValue: (Float) -> String,
) {
    if (games.isEmpty()) {
        Text("Không có dữ liệu", fontSize = 11.sp, color = ChartColors.TextSecondary)
        return
    }
    val allRows = games.map { it to valueOf(it) }
    val sortedDesc = allRows.sortedByDescending { it.second }
    val totalAll = allRows.sumOf { it.second.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val maxVal = sortedDesc.first().second.coerceAtLeast(0.0001f)

    // Sort direction quyết định rank hiển thị và thứ tự render.
    val rows = if (ascending) sortedDesc.reversed().take(limit) else sortedDesc.take(limit)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEachIndexed { i, (g, value) ->
            val pct = value / totalAll * 100f
            // Rank = vị trí thực trong xếp hạng giảm dần.
            val rank = if (ascending) sortedDesc.size - i else i + 1

            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "#$rank",
                    fontSize = 11.sp,
                    color = ChartColors.TextSecondary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(24.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.width(170.dp)
                ) {
                    Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(g.color))
                    Text(g.name, fontSize = 12.sp, color = ChartColors.TextPrimary, maxLines = 1)
                }
                Box(
                    Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(5.dp))
                        .background(g.color.copy(alpha = 0.12f))
                ) {
                    Box(
                        Modifier.fillMaxWidth((value / maxVal).coerceIn(0f, 1f))
                            .fillMaxHeight().clip(RoundedCornerShape(5.dp)).background(g.color)
                    )
                }
                Text(
                    formatValue(value),
                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                    color = ChartColors.TextPrimary, modifier = Modifier.width(80.dp)
                )
                Text(
                    "%.1f%%".format(pct),
                    fontSize = 11.sp, color = ChartColors.TextSecondary,
                    modifier = Modifier.width(50.dp)
                )
            }
        }
    }
}

@Composable
fun SortDirectionToggle(ascending: Boolean, onToggle: () -> Unit) {
    Surface(
        color = Color(0xFFF1EFE8),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(Modifier.size(12.dp)) {
                val sw = 1.4.dp.toPx()
                val mid = size.width / 2f
                if (ascending) {
                    // ↑ tăng dần
                    drawLine(ChartColors.TextPrimary, Offset(mid, size.height),
                        Offset(mid, 0f), sw, cap = StrokeCap.Round)
                    drawLine(ChartColors.TextPrimary, Offset(0f, size.height * 0.35f),
                        Offset(mid, 0f), sw, cap = StrokeCap.Round)
                    drawLine(ChartColors.TextPrimary, Offset(size.width, size.height * 0.35f),
                        Offset(mid, 0f), sw, cap = StrokeCap.Round)
                } else {
                    // ↓ giảm dần
                    drawLine(ChartColors.TextPrimary, Offset(mid, 0f),
                        Offset(mid, size.height), sw, cap = StrokeCap.Round)
                    drawLine(ChartColors.TextPrimary, Offset(0f, size.height * 0.65f),
                        Offset(mid, size.height), sw, cap = StrokeCap.Round)
                    drawLine(ChartColors.TextPrimary, Offset(size.width, size.height * 0.65f),
                        Offset(mid, size.height), sw, cap = StrokeCap.Round)
                }
            }
            Text(
                text = if (ascending) "Tăng dần" else "Giảm dần",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = ChartColors.TextPrimary
            )
        }
    }
}

@Composable
fun TrendLineChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    tooltipTitle: ((Int) -> String)? = null,
    tooltipBody: ((Int, Float) -> String)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var t by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(if (t) 1f else 0f, tween(700, easing = EaseOutCubic), label = "tl")
    LaunchedEffect(values) { t = false; t = true }

    val padLeftDp = 32.dp
    val padRightDp = 8.dp
    val padTopDp = 6.dp
    val padBottomDp = 20.dp

    val n = values.size
    val nmRemember = remember(values) { niceMax(values.maxOrNull() ?: 1f) }
    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(values) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move, PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val pL = with(density) { padLeftDp.toPx() }
                                    val pR = with(density) { padRightDp.toPx() }
                                    val pT = with(density) { padTopDp.toPx() }
                                    val pB = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - pL - pR
                                    val ch = canvasSize.height - pT - pB
                                    val inside = pos.x in (pL - 12f)..(pL + cw + 12f) &&
                                                 pos.y in (pT - 8f)..(pT + ch + 8f)
                                    if (cw > 0f && inside && n > 0) {
                                        val xStep = cw / (n - 1).coerceAtLeast(1)
                                        val idx = ((pos.x - pL) / xStep + 0.5f).toInt()
                                            .coerceIn(0, n - 1)
                                        hoverIdx = idx
                                    } else hoverIdx = null
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val pL = padLeftDp.toPx(); val pR = padRightDp.toPx()
            val pT = padTopDp.toPx();  val pB = padBottomDp.toPx()
            val cW = size.width - pL - pR; val cH = size.height - pT - pB
            val ax = TextStyle(fontSize = 8.sp, color = ChartColors.TextSecondary)
            val lx = TextStyle(fontSize = 9.sp, color = ChartColors.TextSecondary)
            val nm = nmRemember

            repeat(3) { i ->
                val y = pT + cH * (1 - (i + 1) / 3f)
                drawLine(ChartColors.GridLine, Offset(pL, y), Offset(pL + cW, y), 0.5.dp.toPx())
                val l = textMeasurer.measure("%.1f".format(nm * (i + 1) / 3), ax)
                drawText(l, topLeft = Offset(pL - l.size.width - 3.dp.toPx(), y - l.size.height / 2))
            }
            labels.forEachIndexed { i, lbl ->
                if (lbl.isNotEmpty()) {
                    val l = textMeasurer.measure(lbl, lx)
                    val x = pL + i * (cW / (n - 1).coerceAtLeast(1))
                    drawText(l, topLeft = Offset(x - l.size.width / 2, pT + cH + 4.dp.toPx()))
                }
            }

            if (n == 0 || cW <= 0f || cH <= 0f) return@Canvas
            val xStep = cW / (n - 1).coerceAtLeast(1)
            val pts = values.mapIndexed { i, v -> Offset(pL + i * xStep, pT + cH - (v / nm) * cH) }
            val vis = (pts.size * anim).toInt().coerceAtLeast(2).coerceAtMost(pts.size)
            val vp  = pts.take(vis)
            val path = smoothPath(vp)

            val fill = Path().apply {
                addPath(path); lineTo(vp.last().x, pT + cH); lineTo(vp.first().x, pT + cH); close()
            }
            drawPath(fill, Brush.verticalGradient(
                listOf(color.copy(alpha = 0.18f), color.copy(alpha = 0f)), pT, pT + cH))
            drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Marker chốt cuối series.
            if (vp.isNotEmpty()) {
                drawCircle(Color.White, 3.5.dp.toPx(), vp.last())
                drawCircle(color, 2.5.dp.toPx(), vp.last(), style = Stroke(1.5.dp.toPx()))
            }

            // Hover guide line + emphasized dot.
            hoverIdx?.let { idx ->
                if (idx in 0 until n) {
                    val x = pL + idx * xStep
                    drawLine(
                        color = color.copy(alpha = 0.35f),
                        start = Offset(x, pT),
                        end = Offset(x, pT + cH),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                    val y = pT + cH - (values[idx] / nm) * cH
                    drawCircle(Color.White, 5.dp.toPx(), Offset(x, y))
                    drawCircle(color, 5.dp.toPx(), Offset(x, y), style = Stroke(2.dp.toPx()))
                }
            }
        }

        // Tooltip overlay.
        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || n == 0) return@let
            if (idx !in 0 until n) return@let
            val titleFn = tooltipTitle ?: return@let
            val bodyFn = tooltipBody ?: return@let

            val pLpx = with(density) { padLeftDp.toPx() }
            val pRpx = with(density) { padRightDp.toPx() }
            val cw = canvasSize.width - pLpx - pRpx
            if (cw <= 0f) return@let
            val xStep = cw / (n - 1).coerceAtLeast(1)
            val pxX = pLpx + idx * xStep
            val xDp = with(density) { pxX.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 160.dp
            val placeLeft = (xDp + 12.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 8.dp) else (xDp + 8.dp)
            val offsetY = 4.dp

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = titleFn(idx),
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    Text(
                        text = bodyFn(idx, values[idx]),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun CompareLineChart(
    primary: List<Float>,
    comparison: List<Float>,
    labels: List<String>,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    primaryName: String = "Trò chơi",
    comparisonName: String = "TB công viên",
    tooltipTitle: ((Int) -> String)? = null,
    tooltipFormat: ((Float) -> String)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var t by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(if (t) 1f else 0f, tween(700, easing = EaseOutCubic), label = "cl")
    LaunchedEffect(primary) { t = false; t = true }

    val padLeftDp = 34.dp
    val padRightDp = 8.dp
    val padTopDp = 6.dp
    val padBottomDp = 22.dp

    val n = labels.size
    val nmRemember = remember(primary, comparison) {
        niceMax((primary + comparison).maxOrNull() ?: 1f)
    }
    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .pointerInput(primary, comparison) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Move, PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val pL = with(density) { padLeftDp.toPx() }
                                    val pR = with(density) { padRightDp.toPx() }
                                    val pT = with(density) { padTopDp.toPx() }
                                    val pB = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - pL - pR
                                    val ch = canvasSize.height - pT - pB
                                    val inside = pos.x in (pL - 12f)..(pL + cw + 12f) &&
                                                 pos.y in (pT - 8f)..(pT + ch + 8f)
                                    if (cw > 0f && inside && n > 0) {
                                        val xStep = cw / (n - 1).coerceAtLeast(1)
                                        val idx = ((pos.x - pL) / xStep + 0.5f).toInt()
                                            .coerceIn(0, n - 1)
                                        hoverIdx = idx
                                    } else hoverIdx = null
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val pL = padLeftDp.toPx(); val pR = padRightDp.toPx()
            val pT = padTopDp.toPx();  val pB = padBottomDp.toPx()
            val cW = size.width - pL - pR; val cH = size.height - pT - pB
            val nm = nmRemember
            val ax = TextStyle(fontSize = 9.sp, color = ChartColors.TextSecondary)
            val lx = TextStyle(fontSize = 10.sp, color = ChartColors.TextSecondary)

            repeat(4) { i ->
                val y = pT + cH * (1 - (i + 1) / 4f)
                drawLine(ChartColors.GridLine, Offset(pL, y), Offset(pL + cW, y), 0.5.dp.toPx())
                val l = textMeasurer.measure("%.0f".format(nm * (i + 1) / 4), ax)
                drawText(l, topLeft = Offset(pL - l.size.width - 4.dp.toPx(), y - l.size.height / 2))
            }
            labels.forEachIndexed { i, lbl ->
                val x = pL + i * (cW / (n - 1).coerceAtLeast(1))
                val l = textMeasurer.measure(lbl, lx)
                drawText(l, topLeft = Offset(x - l.size.width / 2, pT + cH + 5.dp.toPx()))
            }

            if (n == 0 || cW <= 0f || cH <= 0f) return@Canvas
            val xStep = cW / (n - 1).coerceAtLeast(1)

            fun drawSeries(vals: List<Float>, color: Color, dashed: Boolean, fill: Boolean) {
                val pts = vals.mapIndexed { i, v -> Offset(pL + i * xStep, pT + cH - (v / nm) * cH) }
                val vis = (pts.size * anim).toInt().coerceAtLeast(2).coerceAtMost(pts.size)
                val vp  = pts.take(vis)
                val path = smoothPath(vp)
                if (fill) {
                    val fp = Path().apply {
                        addPath(path); lineTo(vp.last().x, pT + cH); lineTo(vp.first().x, pT + cH); close()
                    }
                    drawPath(fp, Brush.verticalGradient(
                        listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0f)), pT, pT + cH))
                }
                drawPath(path, color, style = Stroke(
                    if (dashed) 1.5.dp.toPx() else 2.dp.toPx(),
                    pathEffect = if (dashed) PathEffect.dashPathEffect(floatArrayOf(5f, 4f)) else null,
                    cap = StrokeCap.Round, join = StrokeJoin.Round))
                if (vp.isNotEmpty()) {
                    drawCircle(Color.White, 3.5.dp.toPx(), vp.last())
                    drawCircle(color, 2.5.dp.toPx(), vp.last(), style = Stroke(1.5.dp.toPx()))
                }
            }
            drawSeries(comparison, ChartColors.Gray400, dashed = true, fill = false)
            drawSeries(primary, primaryColor, dashed = false, fill = true)

            // Hover guide line + dots cho cả 2 series.
            hoverIdx?.let { idx ->
                if (idx in 0 until n) {
                    val x = pL + idx * xStep
                    drawLine(
                        color = ChartColors.TextSecondary.copy(alpha = 0.4f),
                        start = Offset(x, pT),
                        end = Offset(x, pT + cH),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
                    )
                    val yP = pT + cH - (primary.getOrElse(idx) { 0f } / nm) * cH
                    val yC = pT + cH - (comparison.getOrElse(idx) { 0f } / nm) * cH
                    drawCircle(Color.White, 5.dp.toPx(), Offset(x, yC))
                    drawCircle(ChartColors.Gray400, 5.dp.toPx(), Offset(x, yC), style = Stroke(2.dp.toPx()))
                    drawCircle(Color.White, 5.dp.toPx(), Offset(x, yP))
                    drawCircle(primaryColor, 5.dp.toPx(), Offset(x, yP), style = Stroke(2.dp.toPx()))
                }
            }
        }

        // Tooltip overlay 2 dòng: primary + comparison.
        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || n == 0) return@let
            if (idx !in 0 until n) return@let
            val titleFn = tooltipTitle ?: { i: Int -> labels.getOrElse(i) { "" } }
            val fmt = tooltipFormat ?: { v: Float -> v.toString() }

            val pLpx = with(density) { padLeftDp.toPx() }
            val pRpx = with(density) { padRightDp.toPx() }
            val cw = canvasSize.width - pLpx - pRpx
            if (cw <= 0f) return@let
            val xStep = cw / (n - 1).coerceAtLeast(1)
            val pxX = pLpx + idx * xStep
            val xDp = with(density) { pxX.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 200.dp
            val placeLeft = (xDp + 12.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 8.dp) else (xDp + 8.dp)
            val offsetY = 4.dp

            val pV = primary.getOrElse(idx) { 0f }
            val cV = comparison.getOrElse(idx) { 0f }

            Surface(
                modifier = Modifier.offset(x = offsetX.coerceAtLeast(4.dp), y = offsetY),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0x1A888780)),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = titleFn(idx),
                        fontSize = 11.sp,
                        color = ChartColors.TextSecondary
                    )
                    Text(
                        text = "$primaryName: ${fmt(pV)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryColor
                    )
                    Text(
                        text = "$comparisonName: ${fmt(cV)}",
                        fontSize = 11.sp,
                        color = ChartColors.Gray400
                    )
                }
            }
        }
    }
}

@Composable
fun DowBarChart(values: List<Float>, labels: List<String>, color: Color, modifier: Modifier = Modifier) {
    InteractiveBarChart(
        values = values,
        labels = labels,
        color = color,
        modifier = modifier,
        labelFontSp = 10,
        barWidthRatio = 0.62f,
        peakHighlight = true,
        tooltipTitle = { i -> "Thứ ${labels[i]}" },
        tooltipBody = { _, v -> "${v.toInt()} lượt" }
    )
}

@Composable
fun HourBarChart(values: List<Float>, labels: List<String>, color: Color, modifier: Modifier = Modifier) {
    InteractiveBarChart(
        values = values,
        labels = labels,
        color = color,
        modifier = modifier,
        labelFontSp = 9,
        barWidthRatio = 0.7f,
        peakHighlight = false,
        tooltipTitle = { i -> labels[i] },
        tooltipBody = { _, v -> "${v.toInt()} lượt TB" }
    )
}

/**
 * Bar chart đơn series với hover tooltip. Dùng chung cho DowBarChart & HourBarChart.
 * peakHighlight=true → cột max sáng, các cột còn lại mờ; peakHighlight=false → tô đậm theo ratio.
 */
@Composable
private fun InteractiveBarChart(
    values: List<Float>,
    labels: List<String>,
    color: Color,
    modifier: Modifier,
    labelFontSp: Int,
    barWidthRatio: Float,
    peakHighlight: Boolean,
    tooltipTitle: (Int) -> String,
    tooltipBody: (Int, Float) -> String,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    var trigger by remember { mutableStateOf(false) }
    val anim by animateFloatAsState(
        if (trigger) 1f else 0f, tween(600, easing = EaseOutCubic), label = "ibar"
    )
    LaunchedEffect(values) { trigger = false; trigger = true }

    val padLeftDp = 6.dp
    val padRightDp = 6.dp
    val padTopDp = 4.dp
    val padBottomDp = 22.dp

    val n = values.size
    val maxV = values.maxOrNull() ?: 1f
    val peakIdx = values.indexOf(maxV)

    var hoverIdx by remember { mutableStateOf<Int?>(null) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }

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
                                PointerEventType.Move, PointerEventType.Enter -> {
                                    val pos = event.changes.firstOrNull()?.position ?: continue
                                    val pL = with(density) { padLeftDp.toPx() }
                                    val pR = with(density) { padRightDp.toPx() }
                                    val pT = with(density) { padTopDp.toPx() }
                                    val pB = with(density) { padBottomDp.toPx() }
                                    val cw = canvasSize.width - pL - pR
                                    val ch = canvasSize.height - pT - pB
                                    val inside = pos.x in (pL - 8f)..(pL + cw + 8f) &&
                                                 pos.y in (pT - 8f)..(pT + ch + 8f)
                                    if (cw > 0f && inside && n > 0) {
                                        val gW = cw / n
                                        val idx = ((pos.x - pL) / gW).toInt().coerceIn(0, n - 1)
                                        hoverIdx = idx
                                    } else hoverIdx = null
                                }
                                PointerEventType.Exit -> hoverIdx = null
                            }
                        }
                    }
                }
        ) {
            val pL = padLeftDp.toPx(); val pR = padRightDp.toPx()
            val pT = padTopDp.toPx();  val pB = padBottomDp.toPx()
            val cW = size.width - pL - pR; val cH = size.height - pT - pB
            if (n == 0 || cW <= 0f || cH <= 0f) return@Canvas
            val lx = TextStyle(fontSize = labelFontSp.sp, color = ChartColors.TextSecondary)
            val gW = cW / n
            val bW = gW * barWidthRatio
            val cornerR = 3.dp.toPx()

            values.forEachIndexed { i, v ->
                val ratio = v / maxV
                val bH = ratio * cH * anim
                val x  = pL + i * gW + (gW - bW) / 2f
                val c  = if (peakHighlight) {
                    if (i == peakIdx) color else color.copy(alpha = 0.3f)
                } else {
                    when {
                        ratio > 0.75f -> color
                        ratio > 0.4f  -> color.copy(alpha = 0.6f)
                        else          -> color.copy(alpha = 0.25f)
                    }
                }
                drawRoundRect(color.copy(alpha = 0.07f), Offset(x, pT), Size(bW, cH), CornerRadius(cornerR))
                drawRoundRect(c, Offset(x, pT + cH - bH), Size(bW, bH), CornerRadius(cornerR))
                val l = textMeasurer.measure(labels[i], lx)
                drawText(l, topLeft = Offset(pL + i * gW + gW / 2f - l.size.width / 2f, pT + cH + 4.dp.toPx()))
            }

            hoverIdx?.let { idx ->
                if (idx in 0 until n) {
                    val x = pL + idx * gW + (gW - bW) / 2f
                    val ratio = values[idx] / maxV
                    val bH = ratio * cH * anim
                    val top = pT + cH - bH
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, top),
                        size = Size(bW, bH),
                        cornerRadius = CornerRadius(cornerR),
                        style = Stroke(1.5.dp.toPx())
                    )
                }
            }
        }

        hoverIdx?.let { idx ->
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@let
            if (idx !in 0 until n) return@let
            val pLpx = with(density) { padLeftDp.toPx() }
            val pRpx = with(density) { padRightDp.toPx() }
            val cw = canvasSize.width - pLpx - pRpx
            if (cw <= 0f) return@let

            val gW = cw / n
            val pxX = pLpx + idx * gW + gW / 2f
            val xDp = with(density) { pxX.toDp() }
            val canvasWDp = with(density) { canvasSize.width.toDp() }

            val tooltipApproxW = 140.dp
            val placeLeft = (xDp + 16.dp + tooltipApproxW) > canvasWDp
            val offsetX = if (placeLeft) (xDp - tooltipApproxW - 10.dp) else (xDp + 10.dp)
            val offsetY = 4.dp

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

// ─────────────────────────────────────────────────────────────────────────────
// Time range selector + date range picker dialog
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun TimeRangeSelector(current: TimeRange, onSelect: (TimeRange) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }
    val presets = listOf(
        TimeRange.Last7Days to "7d",
        TimeRange.Last30Days to "30d",
        TimeRange.Last90Days to "90d",
        TimeRange.Last1Year to "1y",
    )

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {

        Surface(color = Color(0xFFF1EFE8), shape = RoundedCornerShape(8.dp)) {
            Row(modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                presets.forEach { (r, lbl) ->
                    val isOn = current == r
                    Surface(
                        color = if (isOn) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        border = if (isOn) BorderStroke(0.5.dp, Color(0x1A888780)) else null,
                        modifier = Modifier.clickable { onSelect(r) }
                    ) {
                        Text(
                            lbl,
                            fontSize = 12.sp,
                            fontWeight = if (isOn) FontWeight.Medium else FontWeight.Normal,
                            color = if (isOn) ChartColors.TextPrimary else ChartColors.TextSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Surface(
            color = Color(0xFFF1EFE8),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable { showPicker = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Canvas(Modifier.size(14.dp)) {
                    val r = 2.dp.toPx()
                    drawRoundRect(
                        color = ChartColors.TextSecondary,
                        topLeft = Offset(0f, 3.dp.toPx()),
                        size = Size(size.width, size.height - 3.dp.toPx()),
                        cornerRadius = CornerRadius(r),
                        style = Stroke(1.dp.toPx())
                    )
                    drawLine(
                        color = ChartColors.TextSecondary,
                        start = Offset(0f, 5.5.dp.toPx()),
                        end = Offset(size.width, 5.5.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = ChartColors.TextSecondary,
                        start = Offset(3.5.dp.toPx(), 0f),
                        end = Offset(3.5.dp.toPx(), 4.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                    drawLine(
                        color = ChartColors.TextSecondary,
                        start = Offset(size.width - 3.5.dp.toPx(), 0f),
                        end = Offset(size.width - 3.5.dp.toPx(), 4.dp.toPx()),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
                val today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
                Text(
                    "${current.fromDate(today).format(DATE_FMT)} – ${current.toDate(today).format(DATE_FMT)}",
                    fontSize = 12.sp, color = ChartColors.TextPrimary
                )
            }
        }
    }

    if (showPicker) {
        DateRangePickerDialog(
            initial = current,
            onDismiss = { showPicker = false },
            onConfirm = { from, to ->
                onSelect(TimeRange.Custom(from, to))
                showPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(
    initial: TimeRange,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val today = LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"))
    var fromRaw by remember { mutableStateOf(initial.fromDate(today).toRawMask()) }
    var toRaw by remember { mutableStateOf(initial.toDate(today).toRawMask()) }
    var showCalendar by remember { mutableStateOf(false) }

    val fromDate = parseDdMmYyyy(renderRawMask(fromRaw))
    val toDate = parseDdMmYyyy(renderRawMask(toRaw))
    val fromError = fromRaw.any { it == '_' }.not() && fromDate == null
    val toError = toRaw.any { it == '_' }.not() && toDate == null
    val rangeError = fromDate != null && toDate != null && toDate.isBefore(fromDate)
    val canConfirm = fromDate != null && toDate != null && !rangeError

    val calendarState = if (showCalendar) {
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = fromDate?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()?.toEpochMilli(),
            initialSelectedEndDateMillis = toDate?.atStartOfDay(ZoneOffset.UTC)
                ?.toInstant()?.toEpochMilli(),
            initialDisplayMode = androidx.compose.material3.DisplayMode.Picker
        )
    } else null

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = canConfirm,
                onClick = {
                    val f = fromDate ?: return@TextButton
                    val t = toDate ?: return@TextButton
                    onConfirm(f, t)
                }
            ) { Text("Áp dụng") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    ) {
        Column(Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Chọn khoảng ngày", fontSize = 16.sp,
                fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DateMaskInput(
                    raw = fromRaw,
                    onRawChange = { fromRaw = it },
                    label = "Từ ngày",
                    isError = fromError,
                    supportingText = if (fromError) "Ngày không hợp lệ" else null,
                    modifier = Modifier.weight(1f)
                )
                DateMaskInput(
                    raw = toRaw,
                    onRawChange = { toRaw = it },
                    label = "Đến ngày",
                    isError = toError || rangeError,
                    supportingText = when {
                        toError -> "Ngày không hợp lệ"
                        rangeError -> "Đến ngày phải >= Từ ngày"
                        else -> null
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(onClick = { showCalendar = !showCalendar }) {
                Text(if (showCalendar) "Đóng lịch" else "📅 Mở lịch")
            }

            if (showCalendar && calendarState != null) {
                DateRangePicker(
                    state = calendarState,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 360.dp, max = 460.dp),
                    title = null,
                    headline = null,
                    showModeToggle = false
                )
                TextButton(
                    enabled = calendarState.selectedStartDateMillis != null,
                    onClick = {
                        val s = calendarState.selectedStartDateMillis ?: return@TextButton
                        val e = calendarState.selectedEndDateMillis ?: s
                        val f = millisToLocalDate(s)
                        val tRaw = millisToLocalDate(e)
                        val t = if (tRaw.isBefore(f)) f else tRaw
                        fromRaw = f.toRawMask()
                        toRaw = t.toRawMask()
                        showCalendar = false
                    }
                ) { Text("Dùng ngày đã chọn") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Masked dd/MM/yyyy input — giữ vị trí khi xoá ký tự ở giữa.
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DateMaskInput(
    raw: String,
    onRawChange: (String) -> Unit,
    label: String,
    isError: Boolean = false,
    supportingText: String? = null,
    modifier: Modifier = Modifier
) {
    val rendered = renderRawMask(raw)
    var tfv by remember {
        mutableStateOf(TextFieldValue(rendered, TextRange(rendered.length)))
    }
    // Đồng bộ ngược lại khi raw bị thay từ ngoài (vd: chọn từ calendar).
    LaunchedEffect(raw) {
        if (tfv.text != rendered) {
            tfv = TextFieldValue(rendered, TextRange(rendered.length))
        }
    }
    OutlinedTextField(
        value = tfv,
        onValueChange = { new ->
            val (newRaw, newCursor) = applyMaskChange(
                oldRaw = raw,
                oldText = tfv.text,
                newText = new.text,
                newCursor = new.selection.start
            )
            val newRendered = renderRawMask(newRaw)
            val safeCursor = newCursor.coerceIn(0, newRendered.length)
            tfv = TextFieldValue(newRendered, TextRange(safeCursor))
            if (newRaw != raw) onRawChange(newRaw)
        },
        label = { Text(label, fontSize = 12.sp) },
        isError = isError,
        supportingText = supportingText?.let {
            { Text(it, fontSize = 11.sp) }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp,
            color = ChartColors.TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = ChartColors.Blue400,
            unfocusedBorderColor = Color(0x33888780)
        ),
        modifier = modifier
    )
}

// raw luôn dài 8: mỗi vị trí là '0'..'9' hoặc '_'.
private fun renderRawMask(raw: String): String {
    val r = raw.padEnd(8, '_').take(8)
    return "${r.substring(0, 2)}/${r.substring(2, 4)}/${r.substring(4, 8)}"
}

// Vị trí render (0..10) → vị trí digit (0..7), hoặc -1 nếu là '/'.
private fun renderPosToDigit(pos: Int): Int = when (pos) {
    0, 1 -> pos
    2 -> -1            // '/'
    3, 4 -> pos - 1
    5 -> -1            // '/'
    6, 7, 8, 9 -> pos - 2
    else -> -1
}

private fun String.replaceCharAt(index: Int, c: Char): String =
    substring(0, index) + c + substring(index + 1)

/**
 * Áp dụng thay đổi từ TextField vào model raw.
 * Returns (newRaw, newCursorInRender).
 *
 *  - Insert: chỉ chấp nhận digit, ghi đè vào ô digit tương ứng, cursor nhảy tới ô digit kế.
 *  - Delete: ô bị xoá → set '_' (giữ nguyên vị trí các ô khác). Nếu xoá đúng vị trí '/',
 *    coi như xoá digit ngay TRƯỚC '/' (hành vi backspace tự nhiên).
 *  - Replace cùng độ dài: overwrite digit ở phần khác biệt.
 */
private fun applyMaskChange(
    oldRaw: String,
    oldText: String,
    newText: String,
    newCursor: Int
): Pair<String, Int> {
    if (newText == oldText) return oldRaw to newCursor

    val prefixLen = run {
        var i = 0
        val maxI = minOf(oldText.length, newText.length)
        while (i < maxI && oldText[i] == newText[i]) i++
        i
    }
    var raw = oldRaw.padEnd(8, '_').take(8)

    return when {
        newText.length > oldText.length -> {
            // Insert
            val insertedCount = newText.length - oldText.length
            val inserted = newText.substring(prefixLen, prefixLen + insertedCount)
            var pos = prefixLen
            for (ch in inserted) {
                if (!ch.isDigit()) continue
                while (pos < 10 && (pos == 2 || pos == 5)) pos++
                if (pos >= 10) break
                val d = renderPosToDigit(pos)
                if (d in 0..7) {
                    raw = raw.replaceCharAt(d, ch)
                    pos++
                    if (pos == 2 || pos == 5) pos++
                }
            }
            raw to pos.coerceAtMost(10)
        }
        newText.length < oldText.length -> {
            // Delete
            val deletedCount = oldText.length - newText.length
            for (i in 0 until deletedCount) {
                val pos = prefixLen + i
                val d = renderPosToDigit(pos)
                if (d in 0..7) {
                    raw = raw.replaceCharAt(d, '_')
                } else {
                    // Xoá ngay tại '/' → xoá digit trước đó
                    val dBefore = renderPosToDigit(pos - 1)
                    if (dBefore in 0..7) raw = raw.replaceCharAt(dBefore, '_')
                }
            }
            var cursor = prefixLen
            if (cursor == 2 || cursor == 5) cursor--
            raw to cursor.coerceAtLeast(0)
        }
        else -> {
            // Same length but different content: overwrite digits in the diff range
            var pos = prefixLen
            // Find suffix length
            var suffixLen = 0
            while (suffixLen < newText.length - prefixLen &&
                oldText[oldText.length - 1 - suffixLen] ==
                newText[newText.length - 1 - suffixLen]) suffixLen++
            val replacement = newText.substring(prefixLen, newText.length - suffixLen)
            for (ch in replacement) {
                if (!ch.isDigit()) { pos++; continue }
                while (pos < 10 && (pos == 2 || pos == 5)) pos++
                if (pos >= 10) break
                val d = renderPosToDigit(pos)
                if (d in 0..7) raw = raw.replaceCharAt(d, ch)
                pos++
            }
            raw to pos.coerceAtMost(10)
        }
    }
}

private fun parseDdMmYyyy(s: String): LocalDate? =
    if (s.any { it == '_' }) null
    else runCatching { LocalDate.parse(s, DATE_FMT) }.getOrNull()

private fun LocalDate.toRawMask(): String =
    "%02d%02d%04d".format(dayOfMonth, monthValue, year)

private fun millisToLocalDate(ms: Long): LocalDate =
    java.time.Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate()

// ─────────────────────────────────────────────────────────────────────────────
// UI helpers
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun DetailCard(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0x1A888780))) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ChartColors.TextPrimary)
            content()
        }
    }
}

@Composable
fun MiniKpi(label: String, value: String, change: String, up: Boolean?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color(0xFFF1EFE8), shape = RoundedCornerShape(10.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = ChartColors.TextSecondary, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = ChartColors.TextPrimary, lineHeight = 18.sp)
            if (change.isNotEmpty()) {
                val c = when (up) {
                    true -> Color(0xFF3B6D11); false -> Color(0xFFA32D2D); else -> ChartColors.TextSecondary
                }
                Text(change, fontSize = 10.sp, color = c)
            }
        }
    }
}

@Composable
fun HighlightKpi(
    label: String, gameName: String, value: String, color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, fontSize = 10.sp, color = ChartColors.TextSecondary, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Medium,
                color = color, lineHeight = 18.sp)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(color))
                Text(gameName, fontSize = 10.sp, color = ChartColors.TextSecondary, maxLines = 1)
            }
        }
    }
}

@Composable
fun SidebarItem(label: String, sublabel: String, color: Color,
                selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) color.copy(alpha = 0.08f) else Color.Transparent
    val bd = if (selected) BorderStroke(0.5.dp, color.copy(alpha = 0.35f)) else null
    Surface(color = bg, shape = RoundedCornerShape(8.dp), border = bd,
        modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(Modifier.padding(10.dp, 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(7.dp).clip(RoundedCornerShape(2.dp)).background(color))
            Column(Modifier.weight(1f)) {
                Text(label, fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = ChartColors.TextPrimary, maxLines = 1)
                Text(sublabel, fontSize = 10.sp, color = ChartColors.TextSecondary)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Utils
// ─────────────────────────────────────────────────────────────────────────────

private fun niceMax(v: Float): Float {
    if (v <= 0f) return 1f
    val m = Math.pow(10.0, Math.floor(Math.log10(v.toDouble()))).toFloat()
    return when { v / m <= 1f -> 1f; v / m <= 2f -> 2f; v / m <= 5f -> 5f; else -> 10f } * m
}

private fun smoothPath(pts: List<Offset>): Path {
    val p = Path()
    if (pts.isEmpty()) return p
    p.moveTo(pts[0].x, pts[0].y)
    for (i in 1 until pts.size) {
        val a = pts[i - 1]; val b = pts[i]; val cx = (a.x + b.x) / 2
        p.cubicTo(cx, a.y, cx, b.y, b.x, b.y)
    }
    return p
}
