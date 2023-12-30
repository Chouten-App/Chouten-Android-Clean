package com.chouten.app.common

import android.os.Build
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts epoch milliseconds to a formatted time string.
 *
 * @param epochMilli The epoch time in milliseconds.
 * @param pattern The desired pattern for formatting the time (default is "h:mm:ss").
 * @return The formatted time string of [epochMilli] in the pattern of [pattern].
 */
fun epochMillisToTime(epochMilli: Long, pattern: String = "HH:mm:ss"): String {
    val userTimeZone = TimeZone.getDefault()

    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
        DateTimeFormatter.ofPattern(pattern)
            .withLocale(Locale.getDefault())
            .withZone(userTimeZone.toZoneId())
            .format(Instant.ofEpochMilli(epochMilli))
    } else {
        SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = userTimeZone
        }.format(Date(epochMilli))
    }
}