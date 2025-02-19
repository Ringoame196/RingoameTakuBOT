package com.github.ringoame196.manager

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.NotionScheduleData
import net.dv8tion.jda.api.JDA
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class NotificationManager {
    private val notionManager = NotionManager()

    fun scheduleDailyTaskAtMidnight() {
        val scheduler = Executors.newSingleThreadScheduledExecutor()

        // 現在の時刻を取得
        val now = Calendar.getInstance()

        // 次回の0時を計算
        val midnight = Calendar.getInstance()
        midnight.set(Calendar.HOUR_OF_DAY, 0)
        midnight.set(Calendar.MINUTE, 0)
        midnight.set(Calendar.SECOND, 0)
        midnight.set(Calendar.MILLISECOND, 0)

        // 現在が0時を過ぎていれば、次の日の0時に設定
        if (now.after(midnight)) {
            midnight.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 次回0時までの遅延時間をミリ秒単位で計算
        val delay = midnight.timeInMillis - now.timeInMillis

        // 次回0時にタスクを実行し、その後は毎日繰り返し
        scheduler.scheduleAtFixedRate({
            check()
        }, delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)
    }

    fun check() {
        val sessionReminderMessage = "@everyone\n[セッションVC]\n${Data.SESSION_VOICE_CHANNEL_URL}"
        val characterSheetReminderMessage = "キャラシ提出がまだの方は\n提出お願いします。"
        val jda = Data.jda ?: return
        val scheduleDataList = notionManager.acquisitionSchedule() // 呼び出しを1回で済ませる
        // 1日前通知
        checkNotification(jda,1,sessionReminderMessage,scheduleDataList)
        // 1週間前通知
        checkNotification(jda,7,characterSheetReminderMessage,scheduleDataList)
    }

    private fun checkNotification(jda: JDA,period: Int,addingMessage: String,scheduleDataList: List<NotionScheduleData>) {
        // 特定の日程のスケジュール 確認
        for (schedule in scheduleDataList) {
            // 通知ステータスが設定されていない場合 飛ばす
            println(calculateDaysDifference(schedule.datetime))
            if (calculateDaysDifference(schedule.datetime) != (period - 1)) continue
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
        val today = Date() // 今日の日付

        val startLocalDate = today.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val endLocalDate = targetDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        return ChronoUnit.DAYS.between(startLocalDate, endLocalDate).toInt()
    }
}