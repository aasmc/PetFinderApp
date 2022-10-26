package ru.aasmc.petfinderapp.common.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DateTimeUtils {
    fun parse(dateTimeString: String): LocalDateTime = try {
        LocalDateTime.parse(dateTimeString)
    } catch (e: Exception) {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        LocalDateTime.parse(dateTimeString, dateTimeFormatter)
    }
}