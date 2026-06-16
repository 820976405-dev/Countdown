package com.expiryreminder.app.util

import java.util.concurrent.TimeUnit

fun getDaysUntilExpiry(expireTimestamp: Long): Long {
    val calNow = java.util.Calendar.getInstance()
    val calExpire = java.util.Calendar.getInstance().apply { timeInMillis = expireTimestamp }

    // 统一重置到当天零点比较
    for (cal in listOf(calNow, calExpire)) {
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
    }

    val diffMs = calExpire.timeInMillis - calNow.timeInMillis
    return TimeUnit.MILLISECONDS.toDays(diffMs)
}

fun formatExpiryDate(timestamp: Long): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        .format(java.util.Date(timestamp))
    return date
}

fun formatExpiryDateWithWeekday(timestamp: Long): String {
    val date = java.text.SimpleDateFormat("yyyy-MM-dd (EEE)", java.util.Locale.CHINA)
        .format(java.util.Date(timestamp))
    return date
}

fun getDaysText(days: Long): String {
    return when {
        days < 0 -> "已过期${(-days)}天"
        days == 0L -> "今天到期"
        days == 1L -> "明天到期"
        else -> "${days}天后"
    }
}

fun getDaysTextColor(days: Long): String {
    return when {
        days < 0 -> "#86909C"
        days <= 5 -> "#FF4D4F"
        days <= 7 -> "#FF9500"
        else -> "#5FCF80"
    }
}
