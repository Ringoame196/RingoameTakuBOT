package com.github.ringoame196.Events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.github.ringoame196.Managers.DateTimeManager
import com.github.ringoame196.Managers.DiscordManager
import com.github.ringoame196.Managers.FixedTermScheduleManager
import com.github.ringoame196.Managers.ScheduleManager
import com.github.ringoame196.datas.CommandOptions
import com.github.ringoame196.datas.Data
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color


class SlashCommandInteraction : ListenerAdapter() {
    private val discordManager = DiscordManager()
    private val scheduleManager = ScheduleManager()
    private val dateTimeManager = DateTimeManager()
    private val fixedTermScheduleManager = FixedTermScheduleManager()

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val member = e.member ?: return
        if (!canUseCommand(member)) {
            val message = "あなたは、権限を持っていません"
            e.reply(message).queue()
            return
        }

        try {
            when (e.name) {
                SlashCommandConst.TEST_COMMAND -> testCommand(e)
                SlashCommandConst.STOP_COMMAND -> stopCommand(e)
                SlashCommandConst.RESET_COMMAND -> resetCommand(e)
                SlashCommandConst.MAKE_HO_COMMAND -> makeHOCommand(e)
                SlashCommandConst.SCHEDULE_COMMAND -> scheduleCommand(e)
                SlashCommandConst.DELETE_SCHEDULE_COMMAND -> deleteSchedule(e)
                SlashCommandConst.LIST_SCHEDULE_COMMAND -> listSchedule(e)
                SlashCommandConst.CHECK_SCHEDULE_COMMAND -> checkSchedule(e)
                SlashCommandConst.SEND_COMMAND -> send(e)
                SlashCommandConst.EDIT_SCHEDULE_COMMAND -> editSchedule(e)
            }
        } catch (error:Exception) {
            val message = "エラーが発生しました"
            e.reply(message).setEphemeral(true).queue()
            println(error.message)
        }
    }

    private fun canUseCommand(member: Member):Boolean {
        val roleID = Data.ADMIN_ROLE_ID
        return member.isOwner || member.roles.any { it.id.equals(roleID, ignoreCase = true) }
    }

    private fun acquisitionCommandOptions(e:SlashCommandInteractionEvent): CommandOptions {
        return CommandOptions(
            e.getOption(SlashCommandConst.SCENARIO_NAME_OPTION)?.asString,
            e.getOption(SlashCommandConst.HO_NUMBER_OPTION)?.asDouble?.toInt(),
            e.getOption(SlashCommandConst.DAY_OPTION)?.asString,
            e.getOption(SlashCommandConst.TIME_OPTION)?.asString,
            e.getOption(SlashCommandConst.CHANNEL_OPTION)?.asChannel,
            e.getOption(SlashCommandConst.ID_OPTION)?.asInt,
            e.getOption(SlashCommandConst.TEXT_OPTION)?.asString,
            e.getOption(SlashCommandConst.STATUS_OPTION)?.asInt
        )
    }

    private fun testCommand(e: SlashCommandInteractionEvent) {
        val message = "テストだよ！"
        e.reply(message).setEphemeral(true).queue()
    }

    private fun stopCommand(e: SlashCommandInteractionEvent) {
        val jda = e.jda
        val message = "BOTの電源をオフにするよ！"

        e.reply(message).setEphemeral(true).queue({
            // メッセージが正常に送信された後にシャットダウンを実行
            discordManager.shutdown(jda)
        }, {
            it.printStackTrace()
        })
    }

    private fun resetCommand(e: SlashCommandInteractionEvent) {
        val messageCountToLoad = 80 // 取得するメッセージ数
        val textChannel = e.channel // テキストチャンネル
        // コルーチンを使用して非同期処理を実行
        CoroutineScope(Dispatchers.Default).launch {
            val messages = textChannel.history.retrievePast(messageCountToLoad).complete() // 指定数のメッセージを取得

            val startMessage = "${messages.size}のメッセージをリセットします"
            e.reply(startMessage).setEphemeral(true).queue()

            discordManager.deleteMessages(messages) // メッセージをリセット
        }
    }

    private fun makeHOCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild ?: return
        val commandOptions = acquisitionCommandOptions(e)
        val scenarioName = commandOptions.scenarioName ?: return
        val hoNumber = commandOptions.hoNumber ?:return

        discordManager.makeHOChannel(guild,scenarioName,hoNumber)

        val message = "HOチャンネル作成完了しました"
        e.reply(message).setEphemeral(true).queue()
    }

    private fun scheduleCommand(e: SlashCommandInteractionEvent) {
        val commandOptions = acquisitionCommandOptions(e)
        val dayString = commandOptions.day ?: return
        val timeString = commandOptions.time ?: return
        val channel = commandOptions.channel ?: return
        val scenarioName = commandOptions.scenarioName ?: return
        var statusNumber = commandOptions.status ?: return
        val channelID = channel.id
        val dateData = dateTimeManager.convertingDateTime("$dayString $timeString")

        // 日にち 時間が正しい形式か チェックする
        if (!dateTimeManager.isValidDateTime(dateData)) {
            val message = "日にち 時間の表記方法が間違っています"
            e.reply(message).setEphemeral(true).queue()
            return
        }
        if (statusNumber == Data.UN_NOTIFIED_STATUS && dateTimeManager.isWithinOneWeek(dateData)) statusNumber = Data.NOTIFIED_WEEK_STATUS
        scheduleManager.make(scenarioName, dateData, channelID, statusNumber)

        val status = scheduleManager.changeStatus(statusNumber)
        val title = "日程登録"
        val color = Color.BLUE
        val fields = mutableListOf(
            MessageEmbed.Field("シナリオ名",scenarioName,false),
            MessageEmbed.Field("日程",dateData,false),
            MessageEmbed.Field("通知チャンネル","<#${channelID}>",false),
            MessageEmbed.Field("ステータス",status,false)
        )
        val embed = discordManager.makeEmbed(title = title,color = color, fields = fields)
        e.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun deleteSchedule(e: SlashCommandInteractionEvent) {
        val commandOptions = acquisitionCommandOptions(e)
        val id = commandOptions.id ?: return
        scheduleManager.delete(id)
        val title = "スケジュール削除"
        val descriptor = "${id}のスケジュールを削除しました"
        val color = Color.RED

        val embed = discordManager.makeEmbed(title = title, descriptor = descriptor,color = color)
        e.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun listSchedule(e:SlashCommandInteractionEvent) {
        val sqlCommand = "SELECT * FROM ${Data.TABLE_NAME}"
        val scheduleDataList = scheduleManager.acquisitionScheduleValue(sqlCommand)

        if (scheduleDataList.isEmpty()) {
            val message = "現在設定されているスケジュールはありません"
            e.reply(message).queue()
            return
        }
        val replyMessage = "${scheduleDataList.size}個のスケジュールが見つかりました"
        val scheduleEmbeds = mutableListOf<MessageEmbed>()

        for (scheduleData in scheduleDataList) {
            val embed = scheduleManager.makeScheduleEmbed(scheduleData)
            scheduleEmbeds.add(embed)
        }
        e.reply(replyMessage).addEmbeds(scheduleEmbeds).queue()
    }

    private fun checkSchedule(e:SlashCommandInteractionEvent) {
        fixedTermScheduleManager.checkSchedule()
        fixedTermScheduleManager.updateDateMessage()
        scheduleManager.autoDeleteOldSchedule()
        val message = "チェックを開始します"
        e.reply(message).setEphemeral(true).queue()
    }

    private fun send(e:SlashCommandInteractionEvent) {
        val commandOptions = acquisitionCommandOptions(e)
        val message = "メッセージを送信します"
        val channel = e.channel
        val sendMessage = commandOptions.text ?: return
        e.reply(message).setEphemeral(true).queue()
        channel.sendMessage(sendMessage).queue()
    }

    private fun editSchedule(e: SlashCommandInteractionEvent) {
        val commandOptions = acquisitionCommandOptions(e)
        val id = commandOptions.id ?: return
        val dayString = commandOptions.day
        val timeString = commandOptions.time
        val channel = commandOptions.channel
        val scenarioName = commandOptions.scenarioName
        var statusNumber = commandOptions.status

        if (dayString != null && timeString != null) {
            val dateData = dateTimeManager.convertingDateTime("$dayString $timeString")
            // 日にち 時間が正しい形式か チェックする
            if (dateTimeManager.isValidDateTime(dateData)) scheduleManager.update(id,Data.DATE_KEY,dateData)
        }

        if (channel != null) scheduleManager.update(id,Data.CHANNEL_ID_KEY,channel.id)

        if (scenarioName != null) scheduleManager.update(id,Data.SCENARIO_NAME_KEY,scenarioName)

        if (statusNumber != null) scheduleManager.update(id,Data.STATUS_KEY,statusNumber)

        val schedule = scheduleManager.acquisitionIDScheduleValue(id)
        val embed = scheduleManager.makeScheduleEmbed(schedule[0])
        e.replyEmbeds(embed).setEphemeral(true).queue()
    }
}