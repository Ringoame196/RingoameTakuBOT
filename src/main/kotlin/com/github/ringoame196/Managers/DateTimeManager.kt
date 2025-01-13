package com.github.ringoame196.Managers

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class DateTimeManager {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun isValidDateTime(dateTime: String): Boolean {
        return conversionDateTime(dateTime) != null
    }

    fun isWithinOneWeek(dateData:String): Boolean {
        // 文字列からLocalDateTimeを生成
        val dateTime = conversionDateTime(dateData) ?: return false

        // 現在時刻
        val now = LocalDateTime.now()

        // 1週間後の時刻
        val oneWeekLater = now.plusWeeks(1)

        // 日時が現在から1週間以内かをチェック
        return dateTime.isAfter(now) && dateTime.isBefore(oneWeekLater)
    }

    private fun conversionDateTime(dateTime: String): LocalDateTime? {
        return try {
            LocalDateTime.parse(dateTime, formatter)
        } catch (e:DateTimeParseException) {
            null
        }
    }
}