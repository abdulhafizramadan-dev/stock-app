package com.ahr.stock.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun convertUtcToIndonesiaTime(utcDateString: String): String {
    return try {
        val instant = Instant.parse(utcDateString)
        val indonesiaZone = ZoneId.of("Asia/Jakarta")
        val localDateTime = instant.atZone(indonesiaZone).toLocalDateTime()

        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        dateTimeFormatter.format(localDateTime)
    } catch (_: Exception) {
        utcDateString
    }
}

fun formatCrosshairLabel(dateTimeString: String): String {
    return try {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val dateTime = LocalDateTime.parse(dateTimeString, dateTimeFormatter)
        val today = LocalDate.now(ZoneId.of("Asia/Jakarta"))
        val pointDate = dateTime.toLocalDate()

        if (pointDate == today) {
            dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } else {
            dateTime.format(DateTimeFormatter.ofPattern("dd MMM yy HH:mm"))
        }
    } catch (e: Exception) {
        dateTimeString
    }
}

