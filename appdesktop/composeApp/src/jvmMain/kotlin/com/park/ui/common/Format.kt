package com.park.ui.common

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Tiền VND (đơn vị nguồn: đồng)
// Suffix chuẩn: "đ" / "k đ" / "tr đ" / "tỷ đ"
// ─────────────────────────────────────────────────────────────────────────────

fun formatMoneyVi(amountDong: Number): String {
    val v = amountDong.toFloat()
    return when {
        v >= 1_000_000_000f -> "%.1f tỷ đ".format(v / 1_000_000_000f)
        v >= 1_000_000f     -> "%.1f tr đ".format(v / 1_000_000f)
        v >= 1_000f         -> "${(v / 1_000f).toInt()}k đ"
        else                -> "${v.toInt()}đ"
    }
}

fun moneyAxisLabel(amountDong: Float): String = when {
    amountDong >= 1_000_000_000f -> String.format(Locale.US, "%.1f tỷ", amountDong / 1_000_000_000f)
    amountDong >= 1_000_000f     -> String.format(Locale.US, "%.0f tr", amountDong / 1_000_000f)
    amountDong >= 1_000f         -> String.format(Locale.US, "%.0fk", amountDong / 1_000f)
    else                          -> amountDong.toInt().toString()
}

data class MoneyScale(val divisor: Float, val unitLabel: String)

fun pickMoneyScale(maxRawDong: Float): MoneyScale = when {
    maxRawDong >= 1_000_000_000f -> MoneyScale(1_000_000_000f, "tỷ đ")
    maxRawDong >= 1_000_000f     -> MoneyScale(1_000_000f, "tr đ")
    maxRawDong >= 1_000f         -> MoneyScale(1_000f, "k đ")
    else                          -> MoneyScale(1f, "đ")
}

// ─────────────────────────────────────────────────────────────────────────────
// Số đếm (lượt, người, thẻ…)
// ─────────────────────────────────────────────────────────────────────────────

private val viThousandsFormatter = DecimalFormat("#,###", DecimalFormatSymbols(Locale.US))

fun formatCountVi(value: Number): String =
    viThousandsFormatter.format(value.toLong()).replace(',', '.')

fun countAxisLabel(value: Float): String = when {
    value >= 1_000_000f -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
    value >= 1_000f     -> String.format(Locale.US, "%.0fk", value / 1_000f)
    else                -> value.toInt().toString()
}

// ─────────────────────────────────────────────────────────────────────────────
// Phần trăm
// ─────────────────────────────────────────────────────────────────────────────

fun formatPercent(value: Double, decimals: Int = 1): String =
    String.format(Locale.US, "%.${decimals}f%%", value)

fun formatSignedPercent(value: Double, decimals: Int = 1): String {
    val sign = if (value >= 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.${decimals}f", value)}%"
}
