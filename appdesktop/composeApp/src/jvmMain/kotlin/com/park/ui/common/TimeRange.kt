package com.park.ui.common

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

sealed class TimeRange(val days: Int, val label: String) {
    object Last7Days  : TimeRange(7,   "7 ngày")
    object Last30Days : TimeRange(30,  "30 ngày")
    object Last90Days : TimeRange(90,  "90 ngày")
    object Last1Year  : TimeRange(365, "1 năm")
    data class Custom(val from: LocalDate, val to: LocalDate) : TimeRange(
        days = (ChronoUnit.DAYS.between(from, to).toInt() + 1).coerceAtLeast(1),
        label = "${from.format(DATE_FMT)} → ${to.format(DATE_FMT)}"
    )

    fun fromDate(today: LocalDate): LocalDate =
        if (this is Custom) from else today.minusDays((days - 1).toLong())

    fun toDate(today: LocalDate): LocalDate =
        if (this is Custom) to else today
}
