package com.park.ui.statistics

import com.park.data.model.GameDetailDTO
import com.park.ui.common.TimeRange
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class TimeSeries(val labels: List<String>, val values: List<Float>)

internal fun dailyLabels(from: LocalDate, to: LocalDate): List<String> {
    val n = ChronoUnit.DAYS.between(from, to).toInt() + 1
    return (0 until n).map {
        val d = from.plusDays(it.toLong())
        "${d.dayOfMonth}/${d.monthValue}"
    }
}

private fun GameDetailDTO.dailyAnchor(today: LocalDate): LocalDate =
    today.minusDays((revenueDaily.size - 1).toLong())

private fun GameDetailDTO.monthlyAnchor(today: LocalDate): LocalDate =
    today.minusMonths(11).withDayOfMonth(1)

/**
 * Range có thể slice từ daily nếu `from` nằm trong cửa sổ daily.
 * Bỏ check `to <= today` để chấp nhận off-by-one giữa client today và backend today
 * (slice đã có `coerceIn` clamp toIdx an toàn).
 */
private fun GameDetailDTO.canUseDaily(today: LocalDate, from: LocalDate, @Suppress("UNUSED_PARAMETER") to: LocalDate): Boolean {
    if (revenueDaily.isEmpty()) return false
    val anchor = dailyAnchor(today)
    return !from.isBefore(anchor)
}

/** Range nằm trong cửa sổ monthly 12 tháng (tính từ from). */
private fun GameDetailDTO.canUseMonthly(today: LocalDate, from: LocalDate, @Suppress("UNUSED_PARAMETER") to: LocalDate): Boolean {
    if (revenueByMonth.isEmpty()) return false
    val anchor = monthlyAnchor(today)
    return !from.isBefore(anchor)
}

/** Slice daily theo offset thực [from, to]. */
private fun GameDetailDTO.sliceDaily(
    today: LocalDate, from: LocalDate, to: LocalDate
): Pair<List<Double>, List<Int>> {
    val anchor = dailyAnchor(today)
    val fromIdx = ChronoUnit.DAYS.between(anchor, from).toInt().coerceIn(0, revenueDaily.size - 1)
    val toIdx = ChronoUnit.DAYS.between(anchor, to).toInt().coerceIn(0, revenueDaily.size - 1)
    if (toIdx < fromIdx) return emptyList<Double>() to emptyList()
    return revenueDaily.subList(fromIdx, toIdx + 1) to playsDaily.subList(fromIdx, toIdx + 1)
}

/** Slice monthly theo offset từ from..to. */
private fun GameDetailDTO.sliceMonthly(
    today: LocalDate, from: LocalDate, to: LocalDate
): Triple<List<Double>, List<Int>, List<String>> {
    if (monthLabels.isEmpty() || revenueByMonth.isEmpty()) {
        return Triple(emptyList(), emptyList(), emptyList())
    }
    val anchor = monthlyAnchor(today)
    val fromIdx = ((from.year - anchor.year) * 12 + (from.monthValue - anchor.monthValue))
        .coerceIn(0, 11)
    val toIdx = ((to.year - anchor.year) * 12 + (to.monthValue - anchor.monthValue))
        .coerceIn(0, 11)
    if (toIdx < fromIdx) return Triple(emptyList(), emptyList(), emptyList())
    return Triple(
        revenueByMonth.subList(fromIdx, toIdx + 1),
        playsByMonth.subList(fromIdx, toIdx + 1),
        monthLabels.subList(fromIdx, toIdx + 1)
    )
}

fun GameDetailDTO.revenueFor(today: LocalDate, r: TimeRange): Float {
    val from = r.fromDate(today)
    val to = r.toDate(today)
    return when {
        canUseDaily(today, from, to)   -> sliceDaily(today, from, to).first.sum().toFloat()
        canUseMonthly(today, from, to) -> sliceMonthly(today, from, to).first.sum().toFloat()
        else                            -> 0f
    }
}

fun GameDetailDTO.playsFor(today: LocalDate, r: TimeRange): Float {
    val from = r.fromDate(today)
    val to = r.toDate(today)
    return when {
        canUseDaily(today, from, to)   -> sliceDaily(today, from, to).second.sum().toFloat()
        canUseMonthly(today, from, to) -> sliceMonthly(today, from, to).second.sum().toFloat()
        else                            -> 0f
    }
}

/**
 * Bucket theo độ dài range (Cách A):
 *  - n ≤ 31  → daily
 *  - 32..180 → weekly (gộp 7 ngày), label = ngày đầu của tuần "dd/MM"
 *  - > 180   → monthly, label "MM/yyyy"
 */
private fun bucketDailyToSeries(
    from: LocalDate,
    values: List<Double>
): TimeSeries {
    val n = values.size
    return when {
        n <= 31 -> TimeSeries(dailyLabels(from, from.plusDays((n - 1).toLong())),
            values.map { it.toFloat() })
        n <= 180 -> aggregateWeekly(from, values)
        else -> aggregateMonthly(from, values)
    }
}

private fun aggregateWeekly(from: LocalDate, values: List<Double>): TimeSeries {
    val labels = mutableListOf<String>()
    val out = mutableListOf<Float>()
    var i = 0
    while (i < values.size) {
        val end = minOf(i + 7, values.size)
        val startDate = from.plusDays(i.toLong())
        labels.add("%02d/%02d".format(startDate.dayOfMonth, startDate.monthValue))
        out.add(values.subList(i, end).sum().toFloat())
        i = end
    }
    return TimeSeries(labels, out)
}

private fun aggregateMonthly(from: LocalDate, values: List<Double>): TimeSeries {
    val bucketSum = linkedMapOf<String, Double>()
    values.forEachIndexed { idx, v ->
        val d = from.plusDays(idx.toLong())
        val key = "%02d/%04d".format(d.monthValue, d.year)
        bucketSum[key] = (bucketSum[key] ?: 0.0) + v
    }
    return TimeSeries(bucketSum.keys.toList(), bucketSum.values.map { it.toFloat() })
}

fun GameDetailDTO.revenueSeries(today: LocalDate, r: TimeRange): TimeSeries {
    val from = r.fromDate(today)
    val to = r.toDate(today)
    return when {
        canUseDaily(today, from, to) -> {
            val (rev, _) = sliceDaily(today, from, to)
            bucketDailyToSeries(from, rev)
        }
        canUseMonthly(today, from, to) -> {
            val (rev, _, labels) = sliceMonthly(today, from, to)
            TimeSeries(labels, rev.map { it.toFloat() })
        }
        else -> TimeSeries(emptyList(), emptyList())
    }
}

fun GameDetailDTO.playsSeries(today: LocalDate, r: TimeRange): TimeSeries {
    val from = r.fromDate(today)
    val to = r.toDate(today)
    return when {
        canUseDaily(today, from, to) -> {
            val (_, plays) = sliceDaily(today, from, to)
            bucketDailyToSeries(from, plays.map { it.toDouble() })
        }
        canUseMonthly(today, from, to) -> {
            val (_, plays, labels) = sliceMonthly(today, from, to)
            TimeSeries(labels, plays.map { it.toFloat() })
        }
        else -> TimeSeries(emptyList(), emptyList())
    }
}
