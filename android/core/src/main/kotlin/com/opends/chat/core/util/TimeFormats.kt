package com.opends.chat.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object TimeFormats {
    private const val ISO_UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss"
    private val offsetPattern = Regex("""([+-])(\d{2}):?(\d{2})$""")
    private val fractionPattern = Regex("""^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d+))?$""")

    fun parseMillis(raw: String?): Long? {
        if (raw.isNullOrBlank()) return null
        val text = raw.trim()
        val offset = when {
            text.endsWith("Z") -> "Z"
            else -> offsetPattern.find(text)?.let { match ->
                match.groupValues[1] + match.groupValues[2] + match.groupValues[3]
            } ?: return null
        }
        val base = text.removeSuffix("Z").replace(offsetPattern, "")
        val match = fractionPattern.matchEntire(base) ?: return null
        val fraction = (match.groupValues[2] + "000").substring(0, 3)
        val suffix = if (offset == "Z") "Z" else offset
        val pattern = if (offset == "Z") ISO_UTC else "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
        val normalized = "${match.groupValues[1]}.$fraction$suffix"
        return runCatching {
            val format = SimpleDateFormat(pattern, Locale.US)
            format.timeZone = TimeZone.getTimeZone("UTC")
            format.isLenient = false
            format.parse(normalized)?.time
        }.getOrNull()
    }

    fun formatMillis(millis: Long): String {
        val format = SimpleDateFormat(ISO_UTC, Locale.US)
        format.timeZone = TimeZone.getTimeZone("UTC")
        return format.format(Date(millis))
    }

    fun epochSeconds(millis: Long): Double = millis / 1000.0

    fun millisFromEpochSeconds(seconds: Double?): Long? = seconds?.let { (it * 1000.0).toLong() }

    fun nowEpochSeconds(): Double = System.currentTimeMillis() / 1000.0

    fun localWeekdayIndex(millis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun formatDate(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis
        return String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }
}
