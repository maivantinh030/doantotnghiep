package com.park.ui.screen

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class DashboardQuickRange(val label: String) {
    TODAY("Hôm nay"),
    LAST_7_DAYS("7 ngày qua"),
    LAST_30_DAYS("30 ngày qua")
}

enum class StatisticGrouping(val label: String) {
    DAY("Ngày"),
    WEEK("Tuần"),
    MONTH("Tháng")
}

enum class RideStatus(val label: String) {
    ACTIVE("Đang hoạt động"),
    MAINTENANCE("Bảo trì"),
    INACTIVE("Ngừng hoạt động")
}

data class RideStatRow(
    val name: String,
    val area: String,
    val plays: Int,
    val players: Int,
    val revenue: Long,
    val ticketPrice: Int,
    val status: RideStatus
)

data class CardStatusSummary(
    val active: Int,
    val available: Int,
    val blocked: Int
)

data class LineSeriesData(
    val labels: List<String>,
    val values: List<Float>
)

data class KpiData(
    val totalPlayers: Int,
    val totalRevenue: Long,
    val ticketsSold: Int,
    val activeRides: Int,
    val totalRides: Int
)

data class RankedRide(
    val name: String,
    val valueText: String
)

object ParkAdminMockData {
    val rides: List<RideStatRow> = listOf(
        RideStatRow(
            name = "Tàu lượn siêu tốc",
            area = "Adventure Zone",
            plays = 24_800,
            players = 22_500,
            revenue = 1_720_000_000,
            ticketPrice = 120_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Đu quay",
            area = "Family Zone",
            plays = 17_600,
            players = 15_800,
            revenue = 948_000_000,
            ticketPrice = 60_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Hồ nước lớn",
            area = "Water Zone",
            plays = 14_900,
            players = 13_200,
            revenue = 792_000_000,
            ticketPrice = 60_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Nhà ma",
            area = "Mystery Zone",
            plays = 10_950,
            players = 10_200,
            revenue = 918_000_000,
            ticketPrice = 90_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Tháp rơi tự do",
            area = "Extreme Zone",
            plays = 10_430,
            players = 9_700,
            revenue = 873_000_000,
            ticketPrice = 90_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Xe điện đụng",
            area = "Fun Zone",
            plays = 7_540,
            players = 6_920,
            revenue = 346_000_000,
            ticketPrice = 50_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Vòng quay mặt trời",
            area = "Sky Park",
            plays = 7_860,
            players = 7_100,
            revenue = 426_000_000,
            ticketPrice = 60_000,
            status = RideStatus.ACTIVE
        ),
        RideStatRow(
            name = "Mê cung ánh sáng",
            area = "Discovery Zone",
            plays = 0,
            players = 0,
            revenue = 0,
            ticketPrice = 70_000,
            status = RideStatus.INACTIVE
        )
    )

    val cardStatus = CardStatusSummary(
        active = 7,
        available = 4,
        blocked = 0
    )

    val kpi = KpiData(
        totalPlayers = rides.sumOf { it.players },
        totalRevenue = rides.sumOf { it.revenue },
        ticketsSold = rides.sumOf { it.plays },
        activeRides = rides.count { it.status == RideStatus.ACTIVE },
        totalRides = rides.size
    )

    val dashboardWarnings = listOf(
        "Doanh thu hôm nay giảm 8.2% so với hôm qua.",
        "Xe điện đụng có lượt chơi thấp nhất trong nhóm trò đang hoạt động.",
        "Mê cung ánh sáng đang tạm ngừng và chờ lịch bảo trì."
    )

    val top3ByPlayers: List<RankedRide> = rides
        .filter { it.players > 0 }
        .sortedByDescending { it.players }
        .take(3)
        .map { RankedRide(it.name, "${formatNumber(it.players)} lượt") }

    val top5Revenue: List<RankedRide> = rides
        .filter { it.revenue > 0 }
        .sortedByDescending { it.revenue }
        .take(5)
        .map { RankedRide(it.name, formatVndCompact(it.revenue)) }

    val top5LowestPlayers: List<RankedRide> = rides
        .sortedBy { it.players }
        .take(5)
        .map { RankedRide(it.name, "${formatNumber(it.players)} người chơi") }

    private val dayLabels = listOf("27/03", "28/03", "29/03", "30/03", "31/03", "01/04", "02/04")
    private val dayRevenue = listOf(740_000_000f, 810_000_000f, 850_000_000f, 792_000_000f, 890_000_000f, 980_000_000f, 961_000_000f)
    private val dayPlayers = listOf(11_240f, 11_890f, 12_140f, 11_980f, 12_560f, 13_050f, 12_560f)

    private val weekLabels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5", "Tuần 6", "Tuần 7", "Tuần 8")
    private val weekRevenue = listOf(5_180_000_000f, 5_420_000_000f, 5_360_000_000f, 5_740_000_000f, 5_810_000_000f, 5_670_000_000f, 5_950_000_000f, 6_023_000_000f)
    private val weekPlayers = listOf(73_200f, 75_340f, 74_870f, 79_640f, 80_110f, 78_900f, 83_560f, 85_420f)

    private val monthLabels = listOf("T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12", "T1", "T2", "T3", "T4")
    private val monthRevenue = listOf(18_200_000_000f, 19_100_000_000f, 19_800_000_000f, 20_200_000_000f, 21_400_000_000f, 20_900_000_000f, 22_300_000_000f, 23_000_000_000f, 22_100_000_000f, 22_700_000_000f, 23_900_000_000f, 24_600_000_000f)
    private val monthPlayers = listOf(241_000f, 248_300f, 253_100f, 257_400f, 266_000f, 261_700f, 272_900f, 281_300f, 276_500f, 279_800f, 286_900f, 292_600f)

    private val dashboardTodayLabels = listOf("08h", "10h", "12h", "14h", "16h", "18h", "20h", "22h")
    private val dashboardTodayRevenue = listOf(52_000_000f, 76_000_000f, 102_000_000f, 116_000_000f, 138_000_000f, 160_000_000f, 144_000_000f, 126_000_000f)
    private val dashboardTodayPlayers = listOf(820f, 1_180f, 1_460f, 1_790f, 2_010f, 2_340f, 2_120f, 1_930f)

    private val dashboard30DayLabels = listOf("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4")
    private val dashboard30DayRevenue = listOf(5_180_000_000f, 5_810_000_000f, 5_670_000_000f, 6_023_000_000f)
    private val dashboard30DayPlayers = listOf(73_200f, 80_110f, 78_900f, 85_420f)

    fun dashboardRevenueSeries(range: DashboardQuickRange): LineSeriesData {
        return when (range) {
            DashboardQuickRange.TODAY -> LineSeriesData(dashboardTodayLabels, dashboardTodayRevenue)
            DashboardQuickRange.LAST_7_DAYS -> LineSeriesData(dayLabels, dayRevenue)
            DashboardQuickRange.LAST_30_DAYS -> LineSeriesData(dashboard30DayLabels, dashboard30DayRevenue)
        }
    }

    fun dashboardPlayerSeries(range: DashboardQuickRange): LineSeriesData {
        return when (range) {
            DashboardQuickRange.TODAY -> LineSeriesData(dashboardTodayLabels, dashboardTodayPlayers)
            DashboardQuickRange.LAST_7_DAYS -> LineSeriesData(dayLabels, dayPlayers)
            DashboardQuickRange.LAST_30_DAYS -> LineSeriesData(dashboard30DayLabels, dashboard30DayPlayers)
        }
    }

    fun statisticsRevenueSeries(grouping: StatisticGrouping): LineSeriesData {
        return when (grouping) {
            StatisticGrouping.DAY -> LineSeriesData(dayLabels, dayRevenue)
            StatisticGrouping.WEEK -> LineSeriesData(weekLabels, weekRevenue)
            StatisticGrouping.MONTH -> LineSeriesData(monthLabels, monthRevenue)
        }
    }

    fun statisticsPlayerSeries(grouping: StatisticGrouping): LineSeriesData {
        return when (grouping) {
            StatisticGrouping.DAY -> LineSeriesData(dayLabels, dayPlayers)
            StatisticGrouping.WEEK -> LineSeriesData(weekLabels, weekPlayers)
            StatisticGrouping.MONTH -> LineSeriesData(monthLabels, monthPlayers)
        }
    }
}

private val integerFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

fun formatNumber(value: Number): String = integerFormatter.format(value)

fun formatVnd(value: Long): String = "${formatNumber(value)} VND"

fun formatVndCompact(value: Long): String {
    return when {
        value >= 1_000_000_000L -> String.format(Locale.US, "%.2f tỷ VND", value / 1_000_000_000.0)
        value >= 1_000_000L -> String.format(Locale.US, "%.1f triệu VND", value / 1_000_000.0)
        else -> formatVnd(value)
    }
}

fun contributionPercent(value: Long, total: Long): Double {
    if (total <= 0L) return 0.0
    return value.toDouble() * 100.0 / total.toDouble()
}

fun formatPercent(value: Double): String = String.format(Locale.US, "%.1f%%", value)

