package org.example.Managers

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import org.example.Data
import java.awt.Color
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.Timer
import java.util.TimerTask

class ScheduleManager {
    private val databaseManager = DataBaseManager()
    private val discordManager = DiscordManager()

    fun startFixedTermCheck() {
        val timer = Timer()
        val checkTimes = mutableListOf(0,12,20)

        // 次の00分までの遅延時間を計算
        val now = LocalDateTime.now()
        val nextHour = now.plusHours(1).truncatedTo(ChronoUnit.HOURS)
        val delay = java.time.Duration.between(now, nextHour).toMillis()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentHour = LocalDateTime.now().hour
                println(currentHour)
                if (!checkTimes.contains(currentHour)) return
                checkSchedule()
            }
        }, delay, 3600 * 1000) // 3600秒(1時間)ごとに実行
    }

    fun checkSchedule() {
        val jda = Data.jda ?: return
        val oneDay = 1
        val week = 7
        searchSchedule(oneDay,Data.NOTIFIED_WEEK_STATUS,jda)
        searchSchedule(week,Data.UN_NOTIFIED_STATUS,jda)
    }

    private fun searchSchedule(period:Int, status:Int, jda: JDA) {
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME} WHERE ${Data.DATE_KEY} >= DATETIME('now') AND ${Data.DATE_KEY} <= DATETIME('now', '+${period + 1} day') AND ${Data.STATUS_KEY} <= $status;"
        val scheduleDataList = databaseManager.acquisitionScheduleValue(Data.dbFilePath ?: return,sqlCommand)

        val updateSqlCommand = "UPDATE ${Data.TABLE_NAME} SET ${Data.STATUS_KEY} = ${status + 1} WHERE ${Data.DATE_KEY} >= DATETIME('now') AND ${Data.DATE_KEY} <= DATETIME('now', '+${period + 1} day') AND ${Data.STATUS_KEY} <= $status;"
        databaseManager.runSQLCommand(Data.dbFilePath ?: return, updateSqlCommand)

            for (scheduleData in scheduleDataList) {
                val scenarioName = scheduleData.scenarioName
                val datetime = scheduleData.datetime
                val channelId = scheduleData.channelId

                val dateStamp = LocalDateTime.parse(datetime).toEpochSecond(ZoneOffset.UTC)

                val sendChannel = jda.getTextChannelById(channelId) ?: return

                var message = "## [🔔セッション通知]\n「${scenarioName}」のセッションまで${period}日を切ったことをお知らせします。\nセッション日：<t:$dateStamp:f>\n残り：<t:$dateStamp:R>"

                // 1日前の場合のみ メンションをする
                if (period == 1) message = "@everyone\n$message"

                sendChannel.sendMessage(message).queue()
        }
    }

    fun make(scenarioName:String, dateData:String,channelID:String,status:Int) {
        val sqlCommand = "INSERT INTO ${Data.TABLE_NAME} (${Data.SCENARIO_NAME_KEY},${Data.DATE_KEY},${Data.CHANNEL_ID_KEY},${Data.STATUS_KEY}) VALUES (?,?,?,?)"
        databaseManager.runSQLCommand(Data.dbFilePath ?: return,sqlCommand, mutableListOf(scenarioName,dateData,channelID,status))
    }

    fun oldAutoDelete() {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.DATE_KEY} < datetime('now', '-3 days', 'localtime');"
        databaseManager.runSQLCommand(Data.dbFilePath ?: return,sqlCommand)
    }

    fun delete(id:Int) {
        val sqlCommand = "DELETE FROM ${Data.TABLE_NAME} WHERE ${Data.ID_KEY} = ?;"
        databaseManager.runSQLCommand(Data.dbFilePath ?: return,sqlCommand, mutableListOf(id))
    }

    fun sendAllSchedule(e: SlashCommandInteractionEvent) {
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME}"
        val scheduleDataList = databaseManager.acquisitionScheduleValue(Data.dbFilePath ?: return,sqlCommand)

        if (scheduleDataList.size == 0) {
            val message = "現在設定されているスケジュールはありません"
            e.reply(message).queue()
            return
        }

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
            e.replyEmbeds(embed).queue()
        }
    }
}