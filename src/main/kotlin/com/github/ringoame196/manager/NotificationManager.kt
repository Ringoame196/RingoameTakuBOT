package com.github.ringoame196.manager

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.NotionScheduleData
import net.dv8tion.jda.api.JDA
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NotificationManager {
    private val notionManager = NotionManager()

    fun check() {
        val sessionReminderMessage = "@everyone\n## [セッションVC]\n${Data.SESSION_VOICE_CHANNEL_URL}"
        val characterSheetReminderMessage = "キャラシ提出がまだの方は\n提出お願いします。"
        val jda = Data.jda ?: return
        val scheduleDataList = notionManager.acquisitionSchedule() // 呼び出しを1回で済ませる
        // 1日前通知
        checkNotification(jda,1,sessionReminderMessage,scheduleDataList,mutableListOf(Data.NOTIFICATION_ONE_DAYS_AGO,Data.NOTIFICATION_SEVEN_DAYS_AGO))
        // 1週間前通知
        checkNotification(jda,7,characterSheetReminderMessage,scheduleDataList,mutableListOf(Data.NOTIFICATION_SEVEN_DAYS_AGO))
    }

    private fun checkNotification(jda: JDA,period: Int,addingMessage: String,scheduleDataList: List<NotionScheduleData>,targetStatus:List<String>) {
        // 特定の日程のスケジュール 確認
        for (schedule in scheduleDataList) {
            // 通知ステータスが設定されていない場合 飛ばす
            if (!targetStatus.contains(schedule.status)) continue
            val daysDifference = calculateDaysDifference(schedule.datetime)
            if (daysDifference != (period - 1)) continue
            sendSchedule(schedule,jda,period,addingMessage)
        }
    }

    private fun sendSchedule(scheduleData: NotionScheduleData, jda: JDA, period: Int, addingMessage: String) {
        val scenarioName = scheduleData.scenarioName
        val datetime = formatDate(scheduleData.datetime)
        val channelId = scheduleData.channelId

        val sendChannel = jda.getTextChannelById(channelId) ?: return

        var message = "## [🔔セッション通知]\n" +
                "「${scenarioName}」のセッションまで約${period}日なことをお知らせします。\n" +
                "セッション日：$datetime~\n\n" +
                addingMessage

        sendChannel.sendMessage(message).queue()
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return format.format(date)
    }

    private fun calculateDaysDifference(targetDate: Date): Int {
        val zoneId = ZoneId.systemDefault() // システムのデフォルトタイムゾーンを取得

        // 今日の日付（時間情報を削除）
        val todayLocalDate = LocalDate.now(zoneId)

        // targetDate を LocalDate に変換（時間情報を削除）
        val targetLocalDate = targetDate.toInstant().atZone(zoneId).toLocalDate()

        // 日にちだけの差分を計算
        return ChronoUnit.DAYS.between(todayLocalDate, targetLocalDate).toInt()
    }
}