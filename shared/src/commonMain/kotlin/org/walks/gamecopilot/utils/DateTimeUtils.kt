package org.walks.gamecopilot.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


/**
 *  Created by Wing at 14:34 on 2025/6/12
 *
 */
object DateTimeUtils {

    fun getTimeNow(): Long = Clock.System.now().toEpochMilliseconds()
    fun formatTimestamp(millis: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val instant = Instant.fromEpochMilliseconds(millis)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())

        return buildString {
            append(localDateTime.year)
            append('-')
            append(localDateTime.monthNumber.toString().padStart(2, '0'))
            append('-')
            append(localDateTime.dayOfMonth.toString().padStart(2, '0'))
            append(' ')
            append(localDateTime.hour.toString().padStart(2, '0'))
            append(':')
            append(localDateTime.minute.toString().padStart(2, '0'))
            append(':')
            append(localDateTime.second.toString().padStart(2, '0'))
        }
    }
}