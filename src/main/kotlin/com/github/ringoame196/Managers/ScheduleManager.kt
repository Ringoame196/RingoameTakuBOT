package com.github.ringoame196.Managers

import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScheduleData
import java.awt.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ScheduleManager {
    private val databaseManager = DataBaseManager()
    private val discordManager = DiscordManager()

    fun convertingNowTime(): String {
        val now: LocalDateTime = LocalDateTime.now()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return now.format(formatter)
    }

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