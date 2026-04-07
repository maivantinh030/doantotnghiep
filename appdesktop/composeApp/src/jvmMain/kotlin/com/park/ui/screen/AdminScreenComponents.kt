package com.park.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.compose.foundation.Canvas

private val MainBackground = Color(0xFFF4F6FA)
private val AdminCardBackground = Color(0xFFFCFCFE)
private val BorderLight = Color(0xFFE6EAF3)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF667085)

val RevenueColor = Color(0xFFF28A2F)
val PlayerColor = Color(0xFF2E77F4)
val topContentBackground: Color = MainBackground

@Composable
fun DashboardHeader(
    title: String,
    subtitle: String,
    range: DashboardQuickRange,
    onRangeSelected: (DashboardQuickRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DashboardQuickRange.entries.forEach { option ->
                val selected = option == range
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onRangeSelected(option) },
                    color = if (selected) Color(0xFF1D4ED8) else Color(0xFFE9EEF9),
                    contentColor = if (selected) Color.White else TextSecondary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = option.label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = actions)
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    delta: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = delta,
                    fontSize = 12.sp,
                    color = when {
                        delta.startsWith("+") -> Color(0xFF129A58)
                        delta.startsWith("-") -> Color(0xFFD14343)
                        else -> TextSecondary
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ChartCard(
    title: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    options.forEach { option ->
                        val selected = option == selectedOption
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOptionSelected(option) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Color(0xFFFFE8D3) else Color(0xFFF3F5FA),
                            contentColor = if (selected) Color(0xFFB95E1D) else TextSecondary
                        ) {
                            Text(
                                text = option,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
fun InsightCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
fun StatusCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.1f))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = accent,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LineTrendChart(
    series: LineSeriesData,
    lineColor: Color,
    axisFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val max = (series.values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (step in 4 downTo 0) {
                    val value = max * (step / 4f)
                    Text(
                        text = axisFormatter(value),
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (series.values.isEmpty()) return@Canvas

                for (index in 0..4) {
                    val y = size.height * index / 4f
                    drawLine(
                        color = Color(0xFFE6EBF5),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                val stepX = if (series.values.size <= 1) 0f else size.width / (series.values.size - 1)
                val path = Path()

                series.values.forEachIndexed { index, value ->
                    val normalizedY = size.height - (value / max) * size.height
                    val x = stepX * index
                    if (index == 0) {
                        path.moveTo(x, normalizedY)
                    } else {
                        path.lineTo(x, normalizedY)
                    }
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(
                        width = 3f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                series.values.forEachIndexed { index, value ->
                    val normalizedY = size.height - (value / max) * size.height
                    val x = stepX * index
                    drawCircle(
                        color = lineColor,
                        radius = 4f,
                        center = Offset(x, normalizedY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2f,
                        center = Offset(x, normalizedY)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(52.dp))
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                series.labels.forEach { label ->
                    Text(
                        text = label,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BarComparisonChart(
    labels: List<String>,
    values: List<Float>,
    barColor: Color,
    axisFormatter: (Float) -> String,
    modifier: Modifier = Modifier
) {
    val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Column(
                modifier = Modifier
                    .width(52.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                for (step in 4 downTo 0) {
                    val value = max * (step / 4f)
                    Text(
                        text = axisFormatter(value),
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (values.isEmpty()) return@Canvas

                for (index in 0..4) {
                    val y = size.height * index / 4f
                    drawLine(
                        color = Color(0xFFE6EBF5),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                }

                val groupWidth = size.width / values.size
                val barWidth = groupWidth * 0.58f

                values.forEachIndexed { index, value ->
                    val barHeight = size.height * (value / max)
                    val x = groupWidth * index + (groupWidth - barWidth) / 2f
                    val y = size.height - barHeight
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(52.dp))
            Row(modifier = Modifier.weight(1f)) {
                labels.forEach { label ->
                    Text(
                        text = compactLabel(label),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun DonutChartBreakdown(
    entries: List<RideStatRow>,
    modifier: Modifier = Modifier
) {
    val palette = listOf(
        Color(0xFF2F80ED),
        Color(0xFF56CCF2),
        Color(0xFF6FCF97),
        Color(0xFFF2C94C),
        Color(0xFFF2994A),
        Color(0xFFBB6BD9),
        Color(0xFFEB5757)
    )
    val totalPlayers = entries.sumOf { it.players }.coerceAtLeast(1)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(170.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                var start = -90f
                entries.forEachIndexed { index, ride ->
                    val sweep = (ride.players.toFloat() / totalPlayers) * 360f
                    drawArc(
                        color = palette[index % palette.size],
                        startAngle = start,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = 28f)
                    )
                    start += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatNumber(totalPlayers),
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontSize = 20.sp
                )
                Text(
                    text = "người chơi",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            entries.forEachIndexed { index, ride ->
                val pct = if (totalPlayers == 0) 0.0 else ride.players * 100.0 / totalPlayers
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(palette[index % palette.size])
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = ride.name,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatPercent(pct),
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun FilterPanel(
    dateRange: String,
    onDateRangeChange: (String) -> Unit,
    dateOptions: List<String>,
    selectedGame: String,
    onGameChange: (String) -> Unit,
    gameOptions: List<String>,
    selectedArea: String,
    onAreaChange: (String) -> Unit,
    areaOptions: List<String>,
    selectedGrouping: StatisticGrouping,
    onGroupingChange: (StatisticGrouping) -> Unit,
    onApplyFilter: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Bộ lọc dữ liệu",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            BoxWithConstraints {
                val compact = maxWidth < 980.dp

                if (compact) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterDropdown(
                            label = "Khoảng thời gian",
                            selectedOption = dateRange,
                            options = dateOptions,
                            onOptionSelected = onDateRangeChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FilterDropdown(
                            label = "Trò chơi",
                            selectedOption = selectedGame,
                            options = gameOptions,
                            onOptionSelected = onGameChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                        FilterDropdown(
                            label = "Khu vực",
                            selectedOption = selectedArea,
                            options = areaOptions,
                            onOptionSelected = onAreaChange,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterDropdown(
                            label = "Khoảng thời gian",
                            selectedOption = dateRange,
                            options = dateOptions,
                            onOptionSelected = onDateRangeChange,
                            modifier = Modifier.weight(1f)
                        )
                        FilterDropdown(
                            label = "Trò chơi",
                            selectedOption = selectedGame,
                            options = gameOptions,
                            onOptionSelected = onGameChange,
                            modifier = Modifier.weight(1f)
                        )
                        FilterDropdown(
                            label = "Khu vực",
                            selectedOption = selectedArea,
                            options = areaOptions,
                            onOptionSelected = onAreaChange,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatisticGrouping.entries.forEach { grouping ->
                        val selected = grouping == selectedGrouping
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onGroupingChange(grouping) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) Color(0xFF1D4ED8) else Color(0xFFE9EEF9),
                            contentColor = if (selected) Color.White else TextSecondary
                        ) {
                            Text(
                                text = grouping.label,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = onApplyFilter,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D4ED8),
                        contentColor = Color.White
                    )
                ) {
                    Text("Lọc dữ liệu")
                }
            }
        }
    }
}

@Composable
private fun FilterDropdown(
    label: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                    .clickable { expanded = true },
                color = Color.White,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedOption,
                        modifier = Modifier.weight(1f),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 13.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = TextSecondary
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 13.sp) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DataTableSection(
    rows: List<RideStatRow>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedGame: String,
    onGameChange: (String) -> Unit,
    gameOptions: List<String>,
    page: Int,
    size: Int,
    total: Long,
    totalRevenue: Long,
    isLoading: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
    val totalPages = if (total <= 0L) 1L else (total + size - 1L) / size.toLong()
    val startItem = if (total <= 0L) 0L else ((page - 1L) * size + 1L).coerceAtMost(total)
    val endItem = if (total <= 0L) 0L else (startItem + rows.size - 1L).coerceAtMost(total)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AdminCardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Bảng thống kê chi tiết",
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextSecondary
                        )
                    },
                    placeholder = { Text("Tìm theo tên trò chơi hoặc khu vực", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                FilterDropdown(
                    label = "Lọc theo trò",
                    selectedOption = selectedGame,
                    options = gameOptions,
                    onOptionSelected = onGameChange,
                    modifier = Modifier.width(240.dp)
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Đang tải dữ liệu bảng thống kê...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Không có dữ liệu phù hợp với bộ lọc hiện tại",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            } else {
                val horizontalScroll = rememberScrollState()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, BorderLight, RoundedCornerShape(14.dp))
                        .horizontalScroll(horizontalScroll)
                ) {
                    TableHeaderRow()
                    rows.forEachIndexed { index, row ->
                        TableDataRow(
                            index = ((page - 1) * size) + index + 1,
                            row = row,
                            contribution = contributionPercent(row.revenue, totalRevenue)
                        )
                        HorizontalDivider(color = BorderLight)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hiển thị ${startItem}-${endItem} của $total trò chơi",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = onPreviousPage,
                    enabled = page > 1 && !isLoading
                ) {
                    Text("Trước")
                }
                Text(
                    text = "Trang $page / $totalPages",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
                TextButton(
                    onClick = onNextPage,
                    enabled = page.toLong() < totalPages && !isLoading
                ) {
                    Text("Sau")
                }
            }
        }
    }
}
@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .background(Color(0xFFF5F7FC))
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("STT", 56.dp)
        HeaderCell("Tên trò chơi", 200.dp)
        HeaderCell("Khu vực", 140.dp)
        HeaderCell("Lượt chơi", 110.dp)
        HeaderCell("Người chơi", 110.dp)
        HeaderCell("Doanh thu", 150.dp)
        HeaderCell("Giá vé", 120.dp)
        HeaderCell("Doanh thu / lượt", 140.dp)
        HeaderCell("% đóng góp", 100.dp)
        HeaderCell("Trạng thái", 150.dp)
    }
}

@Composable
private fun TableDataRow(
    index: Int,
    row: RideStatRow,
    contribution: Double
) {
    Row(
        modifier = Modifier.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(index.toString(), 56.dp)
        BodyCell(row.name, 200.dp)
        BodyCell(row.area, 140.dp)
        BodyCell(formatNumber(row.plays), 110.dp)
        BodyCell(formatNumber(row.players), 110.dp)
        BodyCell(formatVnd(row.revenue), 150.dp)
        BodyCell(formatVnd(row.ticketPrice.toLong()), 120.dp)
        BodyCell(formatVnd(if (row.plays == 0) 0L else row.revenue / row.plays), 140.dp)
        BodyCell(formatPercent(contribution), 100.dp)
        StatusCell(row.status, 150.dp)
    }
}

@Composable
private fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 10.dp),
        color = TextSecondary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
    )
}

@Composable
private fun BodyCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 10.dp),
        color = TextPrimary,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun StatusCell(status: RideStatus, width: Dp) {
    val (bg, textColor) = when (status) {
        RideStatus.ACTIVE -> Color(0xFFE8F8EE) to Color(0xFF129A58)
        RideStatus.MAINTENANCE -> Color(0xFFFFF5E4) to Color(0xFFD18700)
        RideStatus.INACTIVE -> Color(0xFFFDECEC) to Color(0xFFD14343)
    }

    Box(modifier = Modifier.width(width).padding(horizontal = 10.dp)) {
        Text(
            text = status.label,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AdminPlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.width(500.dp)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Màn hình này đang ở chế độ placeholder. Bạn có thể phát triển tiếp theo luồng nghiệp vụ thực tế.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

fun revenueAxisLabel(value: Float): String {
    return when {
        value >= 1_000_000_000f -> String.format(Locale.US, "%.1f tỷ", value / 1_000_000_000f)
        value >= 1_000_000f -> String.format(Locale.US, "%.0f tr", value / 1_000_000f)
        else -> formatNumber(value.toLong())
    }
}

fun playerAxisLabel(value: Float): String {
    return when {
        value >= 1_000_000f -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
        value >= 1_000f -> String.format(Locale.US, "%.0fk", value / 1_000f)
        else -> formatNumber(value.toLong())
    }
}

private fun compactLabel(label: String): String {
    return when {
        label.length <= 12 -> label
        else -> {
            val pieces = label.split(" ")
            if (pieces.size >= 2) "${pieces.first()}\n${pieces[1]}" else label
        }
    }
}
