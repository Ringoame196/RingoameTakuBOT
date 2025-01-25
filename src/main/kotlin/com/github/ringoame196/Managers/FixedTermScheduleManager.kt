package com.github.ringoame196.Managers

import com.github.ringoame196.datas.Data
import net.dv8tion.jda.api.JDA
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.TimerTask

class FixedTermScheduleManager {
    private val scheduleManager = ScheduleManager()
    private val notificationNotificationManager = NotificationNotificationManager()
    private val dateTimeManager = DateTimeManager()

    fun startFixedTermCheck() {
        val timer = Timer()
        val checkTimes = Data.CHECK_TIME

        // 次の00分までの遅延時間を計算
        val now = LocalDateTime.now()
        val nextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        val delay = Duration.between(now, nextHour).toMillis()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentHour = LocalDateTime.now().hour
                scheduleManager.autoDeleteOldSchedule() // 古いデータを削除する
                updateDateMessage() // スケジュールを更新する
                if (!checkTimes.contains(currentHour)) return // 指定した時間以外は動作を停止
                checkSchedule()
            }
        }, delay, 3600 * 1000) // 3600秒(1時間)ごとに実行
    }

    fun updateDateMessage() {
        val dateChannelID = Data.dateChannelID ?: return
        val dateMessageID = Data.dateMessageID ?: return

        val jda = Data.jda ?: return
        val channel = jda.getTextChannelById(dateChannelID) ?: return
        val newMessage = makeNewMessage()

        try {
            channel.editMessageById(dateMessageID, newMessage).queue()
        } catch (e:Exception) {
            println("[エラー] スケジュールの更新に失敗しました ${e.message}")
        }
    }

    private fun makeNewMessage(): String {
        var messageText = "# [りんご飴卓 スケジュール]"
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME} ORDER BY date_time ASC;"
        val scheduleDataList = scheduleManager.acquisitionScheduleValue(sqlCommand)
        val nowTime = dateTimeManager.convertingNowTime()

        if (scheduleDataList.isEmpty()) {
            messageText = "${messageText}\n現在登録されているスケジュールはありません"
        } else {
            for (scheduleData in scheduleDataList) {
                val scenarioName = scheduleData.scenarioName
                val datetime = scheduleData.datetime
                val channel = scheduleData.channelId
                val remainingTime = scheduleManager.calculateRemainingTime(datetime)
                messageText = "$messageText\n" +
                        "## :watch:[シナリオ名] $scenarioName\n" +
                        "[開催チャンネル] <#$channel>\n" +
                        "[日程] $datetime\n" +
                        "[残り時間] 約$remainingTime"
            }
        }
        messageText = "$messageText\n\n[最終更新] $nowTime"
        return messageText
    }

    fun checkSchedule() {
        val jda = Data.jda ?: return
        val oneDay = 1
        val week = 7
        searchSchedule(oneDay, Data.NOTIFIED_WEEK_STATUS,jda)
        searchSchedule(week, Data.UN_NOTIFIED_STATUS,jda)
    }

    private fun searchSchedule(period:Int, status:Int, jda: JDA) {
        val where = "WHERE ${Data.DATE_KEY} <= DATETIME('now', 'localtime', '+$period days') AND ${Data.STATUS_KEY} <= $status"
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME} $where;"
        val scheduleDataList = scheduleManager.acquisitionScheduleValue(sqlCommand)

        val updateSqlCommand = "UPDATE ${Data.TABLE_NAME} SET ${Data.STATUS_KEY} = ${status + 1} $where;"
        scheduleManager.update(updateSqlCommand)

        for (scheduleData in scheduleDataList) {
            notificationNotificationManager.sendSchedule(scheduleData,jda,period)
        }
    }
}