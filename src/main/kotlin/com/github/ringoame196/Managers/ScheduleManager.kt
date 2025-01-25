package com.github.ringoame196.Managers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScheduleData
import java.awt.Color
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.TimerTask

class ScheduleManager {
    private val databaseManager = DataBaseManager()
    private val discordManager = DiscordManager()

    fun startFixedTermCheck() {
        val timer = Timer()
        val checkTimes = mutableListOf(0,12)

        // 次の00分までの遅延時間を計算
        val now = LocalDateTime.now()
        val nextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        val delay = Duration.between(now, nextHour).toMillis()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentHour = LocalDateTime.now().hour
                autoDeleteOldSchedule()
                updateDateMessage()
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

    private fun convertingNowTime(): String {
        val now: LocalDateTime = LocalDateTime.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return now.format(formatter)
    }

    private fun makeNewMessage(): String {
        var messageText = "# [りんご飴卓 スケジュール]"
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME} ORDER BY date_time ASC;"
        val scheduleDataList = databaseManager.acquisitionScheduleValue(sqlCommand)
        val nowTime = convertingNowTime()

        for (scheduleData in scheduleDataList) {
            val scenarioName = scheduleData.scenarioName
            val datetime = scheduleData.datetime
            val channel = scheduleData.channelId
            val remainingTime = calculateRemainingTime(datetime)
            messageText = "$messageText\n" +
                    "## :watch:[シナリオ名] $scenarioName\n" +
                    "[開催チャンネル] <#$channel>\n" +
                    "[日程] $datetime\n" +
                    "[残り時間] 約$remainingTime"
        }
        messageText = "$messageText\n\n[最終更新] $nowTime"
        return messageText
    }

    private fun calculateRemainingTime(time: String): String {
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
        val scheduleDataList = databaseManager.acquisitionScheduleValue(sqlCommand)

        val updateSqlCommand = "UPDATE ${Data.TABLE_NAME} SET ${Data.STATUS_KEY} = ${status + 1} $where;"
        databaseManager.executeUpdate(updateSqlCommand)

        for (scheduleData in scheduleDataList) {
            sendSchedule(scheduleData,jda,period)
        }
    }

    private fun sendSchedule(scheduleData: ScheduleData, jda: JDA, period: Int) {
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

    fun make(scenarioName:String, dateData:String,channelID:String,status:Int) {
        val sqlCommand = "INSERT INTO ${Data.TABLE_NAME} (${Data.SCENARIO_NAME_KEY},${Data.DATE_KEY},${Data.CHANNEL_ID_KEY},${Data.STATUS_KEY}) VALUES (?,?,?,?)"
        databaseManager.executeUpdate(sqlCommand, mutableListOf(scenarioName,dateData,channelID,status))
    }

    fun autoDeleteOldSchedule() {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.DATE_KEY} < datetime('now', '-1 days', 'localtime');"
        databaseManager.executeUpdate(sqlCommand)
    }

    fun delete(id:Int) {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.ID_KEY} = ?;"
        databaseManager.executeUpdate(sqlCommand, mutableListOf(id))
    }

    fun sendAllSchedule(e: SlashCommandInteractionEvent) {
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME}"
        val scheduleDataList = databaseManager.acquisitionScheduleValue(sqlCommand)

        if (scheduleDataList.isEmpty()) {
            val message = "現在設定されているスケジュールはありません"
            e.reply(message).queue()
            return
        }
        val replyMessage = "${scheduleDataList.size}個のスケジュールが見つかりました"
        val scheduleEmbeds = mutableListOf<MessageEmbed>()

        for (scheduleData in scheduleDataList) {
            val id = scheduleData.id
            val scenarioName = scheduleData.scenarioName
            val datetime = scheduleData.datetime
            val channelId = scheduleData.channelId
            val status = scheduleData.status

            val title = "スケジュール確認"
            val color = Color.YELLOW
            val fields = mutableListOf(
                MessageEmbed.Field("ID","$id",true),
                MessageEmbed.Field("シナリオ名",scenarioName,true),
                MessageEmbed.Field("日程",datetime,true),
                MessageEmbed.Field("送信チャンネル","<#${channelId}>",true),
                MessageEmbed.Field("ステータス","$status",true)
            )

            val embed = discordManager.makeEmbed(title = title,color = color, fields = fields)
            scheduleEmbeds.add(embed)
        }
        e.reply(replyMessage).addEmbeds(scheduleEmbeds).queue()
    }
}