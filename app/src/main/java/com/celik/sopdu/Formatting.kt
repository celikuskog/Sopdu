package com.celik.sopdu

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatStatusTimestamp(ts: Long): String =
    if (ts <= 0L) "not shared" else SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(ts))

internal fun formatLastSeen(lastSeenAt: Long): String {
    if (lastSeenAt <= 0L) return "unknown"
    val diff = (System.currentTimeMillis() - lastSeenAt).coerceAtLeast(0L)
    val sec = diff / 1000
    val min = sec / 60
    val hr = min / 60
    val day = hr / 24
    return when {
        sec < 30 -> "just now"
        min < 2 -> "~1 min ago"
        min < 10 -> "~5 min ago"
        min < 20 -> "~10 min ago"
        min < 60 -> "~30 min ago"
        hr < 2 -> "~1 hr ago"
        hr < 6 -> "~3 hr ago"
        hr < 12 -> "~6 hr ago"
        hr < 24 -> "~12 hr ago"
        day < 3 -> ">24 hr ago"
        else -> ">3 days ago"
    }
}
