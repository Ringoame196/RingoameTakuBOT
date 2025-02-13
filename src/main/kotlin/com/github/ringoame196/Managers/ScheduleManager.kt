package com.github.ringoame196.Managers

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScheduleData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ScheduleManager {
    private val databaseManager = DataBaseManager()

    fun acquisitionScheduleValue(sql: String):MutableList<ScheduleData> {
        return databaseManager.acquisitionScheduleValue(sql)
    }

    fun calculateRemainingTime(time: String): String {
        val now = LocalDateTime.now()
        val targetDate = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        // 日数差を計算
        val daysDifference = ChronoUnit.DAYS.between(now.toLocalDate(), targetDate.toLocalDate())
        // 時間差を計算
        val hoursDifference = ChronoUnit.HOURS.between(now, targetDate)

        return if (daysDifference >= 1) {
            "${daysDifference}日"
        } else {
            "${hoursDifference}時間"
        }
    }

    fun make(scenarioName:String, dateData:String,channelID:String,status:Int) {
        val sqlCommand = "INSERT INTO ${Data.TABLE_NAME} (${Data.SCENARIO_NAME_KEY},${Data.DATE_KEY},${Data.CHANNEL_ID_KEY},${Data.STATUS_KEY}) VALUES (?,?,?,?)"
        databaseManager.executeUpdate(sqlCommand, mutableListOf(scenarioName,dateData,channelID,status))
    }

    fun update(updateSqlCommand: String) {
        databaseManager.executeUpdate(updateSqlCommand)
    }

    fun autoDeleteOldSchedule() {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.DATE_KEY} < datetime('now', '-1 days', 'localtime');"
        databaseManager.executeUpdate(sqlCommand)
    }

    fun delete(id:Int) {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.ID_KEY} = ?;"
        databaseManager.executeUpdate(sqlCommand, mutableListOf(id))
    }

    fun changeStatus(statusNumber: Int):String {
        return when(statusNumber) {
            Data.UN_NOTIFIED_STATUS -> "1週間前通知前"
            Data.NOTIFIED_WEEK_STATUS -> "1日前通知前"
            else -> "通知なし"
        }
    }
}