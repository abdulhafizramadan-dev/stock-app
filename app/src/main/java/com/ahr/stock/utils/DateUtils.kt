package com.ahr.stock.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val indonesiaZone = ZoneId.of("Asia/Jakarta")

fun convertUtcToIndonesiaTime(utcDateString: String): String {
    return try {
        val instant = Instant.parse(utcDateString)
        val localDateTime = instant.atZone(indonesiaZone).toLocalDateTime()
        localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    } catch (_: Exception) {
        utcDateString
    }
}

fun formatCrosshairLabel(dateTimeString: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        val today = LocalDate.now(indonesiaZone)
        if (dateTime.toLocalDate() == today) {
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            dateTime.format(DateTimeFormatter.ofPattern("dd MMM yy HH:mm"))
        }
    } catch (_: Exception) {
        dateTimeString
    }
}

fun formatPublishedAt(utcDateString: String): String {
    return try {
        val instant = Instant.parse(utcDateString)
        val localDateTime = instant.atZone(indonesiaZone).toLocalDateTime()
        val today = LocalDate.now(indonesiaZone)
        val publishedDate = localDateTime.toLocalDate()

        when {
            publishedDate == today ->
                localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            publishedDate == today.minusDays(1) ->
                "Yesterday ${localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            else ->
                localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))
        }
    } catch (_: Exception) {
        utcDateString
    }
}
