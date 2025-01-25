package com.github.ringoame196.Managers

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScheduleData
import net.dv8tion.jda.api.JDA

class NotificationNotification {
    fun sendSchedule(scheduleData: ScheduleData, jda: JDA, period: Int) {
        val scenarioName = scheduleData.scenarioName
        val datetime = scheduleData.datetime
        val channelId = scheduleData.channelId

        val sendChannel = jda.getTextChannelById(channelId) ?: return

        var message = "## [🔔セッション通知]\n" +
                "「${scenarioName}」のセッションまで約${period}日なことをお知らせします。\n" +
                "セッション日：$datetime"

        // 1日前の場合のみ メンションとVCのURLを貼り付ける
        if (period == 1) message = "@everyone\n" +
                "$message\n" +
                Data.SESSION_VOICE_CHANNEL_URL

        sendChannel.sendMessage(message).queue()
    }
}