package com.park.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import com.park.data.model.RevenueChartData
import com.park.ui.theme.AppColors
import com.park.ui.theme.AppTypography

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color = AppColors.WarmOrange,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    fontSize = 13.sp,
                    color = AppColors.PrimaryGray,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.PrimaryDark
                )
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = AppColors.GreenSuccess)
                }
            }
        }
    }
}

@Composable
fun PageHeader(
    title: String,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = AppTypography.headlineLarge, color = AppColors.PrimaryDark)
            if (subtitle != null) {
                Text(subtitle, style = AppTypography.bodyMedium, color = AppColors.PrimaryGray)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Tìm kiếm...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = AppColors.PrimaryGray) },
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.WarmOrange,
            cursorColor = AppColors.WarmOrange
        ),
        modifier = modifier.height(56.dp)
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor) = when (status.uppercase()) {
        "ACTIVE", "SUCCESS", "COMPLETED" -> AppColors.GreenSuccess.copy(alpha = 0.15f) to AppColors.GreenSuccess
        "INACTIVE", "LOCKED", "SUSPENDED" -> AppColors.RedError.copy(alpha = 0.15f) to AppColors.RedError
        "PENDING" -> AppColors.YellowWarning.copy(alpha = 0.15f) to AppColors.YellowWarning
        "MAINTENANCE" -> AppColors.PrimaryGray.copy(alpha = 0.15f) to AppColors.PrimaryGray
        else -> AppColors.BluePrimary.copy(alpha = 0.15f) to AppColors.BluePrimary
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}


@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = AppColors.PrimaryGray, style = AppTypography.bodyMedium)
        Text(value, color = AppColors.PrimaryDark, style = AppTypography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionDivider(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(modifier = Modifier.weight(1f), color = AppColors.LightGray)
        Text(
            title,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = AppColors.PrimaryGray,
            fontSize = 12.sp
        )
        Divider(modifier = Modifier.weight(1f), color = AppColors.LightGray)
    }
}

@Composable
fun SnackbarMessage(
    message: String?,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (message != null) {
        val bgColor = if (isError) AppColors.RedError else AppColors.GreenSuccess
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = bgColor),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                message,
                color = Color.White,
                modifier = Modifier.padding(12.dp, 8.dp),
                fontSize = 14.sp
            )
        }
    }
}

fun formatCurrencyShort(value: Double): String {
    return when {
        value >= 1_000_000_000 -> "${String.format("%.1f", value / 1_000_000_000)}tỷ"
        value >= 1_000_000 -> "${String.format("%.1f", value / 1_000_000)}tr"
        value >= 1_000 -> "${String.format("%.0f", value / 1_000)}k"
        else -> value.toInt().toString()
    }
}

fun formatCurrencyFull(value: Double): String {
    val formatted = String.format("%,.0f", value)
    return "$formatted ₫"
}

@Composable
fun RevenueBarChart(
    data: RevenueChartData,
    modifier: Modifier = Modifier
) {
    if (data.labels.isEmpty() || data.values.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Không có dữ liệu", color = AppColors.PrimaryGray)
        }
        return
    }

    val labelListKey = remember { ExtraStore.Key<List<String>>() }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(data) {
        modelProducer.runTransaction {
            columnSeries { series(data.values) }
            extras { it[labelListKey] = data.labels }
        }
    }

    val bottomAxisFormatter = CartesianValueFormatter { context, x, _ ->
        context.model.extraStore.getOrNull(labelListKey)?.getOrElse(x.toInt()) { "" } ?: ""
    }

    val startAxisFormatter = CartesianValueFormatter { _, y, _ ->
        formatCurrencyShort(y)
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberColumnCartesianLayer(
                columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                    rememberLineComponent(fill = Fill(AppColors.WarmOrange), thickness = 16.dp)
                )
            ),
            startAxis = VerticalAxis.rememberStart(valueFormatter = startAxisFormatter),
            bottomAxis = HorizontalAxis.rememberBottom(valueFormatter = bottomAxisFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier.fillMaxWidth().height(200.dp),
        zoomState = rememberVicoZoomState(zoomEnabled = false),
    )
}