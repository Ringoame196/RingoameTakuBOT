package com.github.ringoame196.Events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.github.ringoame196.Managers.DateTimeManager
import com.github.ringoame196.Managers.DiscordManager
import com.github.ringoame196.Managers.FixedTermScheduleManager
import com.github.ringoame196.Managers.ScheduleManager
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
        if (!canUseCommand(member)) return

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

            resetMessage(messages) // メッセージをリセット
        }
    }

    private suspend fun resetMessage(messages: MutableList<Message>) {
        for (message in messages) {
            if (!message.isPinned && (message.type == MessageType.DEFAULT || message.type == MessageType.INLINE_REPLY || message.type == MessageType.SLASH_COMMAND)) message.delete().queue()
            else if (message.reactions.isNotEmpty()) message.clearReactions().queue() // リアクションリセット
            delay(1000L) // 1秒遅延
        }
    }

    private fun makeHOCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild
        val scenarioName = e.getOption("scenarioname")?.asString ?:return
        val hoNumber = e.getOption("honumber")?.asDouble?.toInt() ?:return

        guild?.createCategory(scenarioName)?.queue { category ->
            // カテゴリー内にテキストチャンネルを作成
            for (i in 1..hoNumber) {
                val hoName = "HO${i}-$scenarioName"

                // ロール作成
                guild.createRole()
                    .setName(hoName)
                    .queue { role ->
                        // チャンネルを作成
                        category.createTextChannel(hoName).queue { textChannel ->
                            // @everyoneに対して、チャンネルが見えないようにする
                            textChannel.upsertPermissionOverride(guild.publicRole)
                                .setDenied(Permission.VIEW_CHANNEL)
                                .queue()

                            // 特定のロールに対して、チャンネルが見えるようにする
                            textChannel.upsertPermissionOverride(role)
                                .setAllowed(Permission.VIEW_CHANNEL)
                                .queue()
                        }
                    }
            }
        }

        val message = "HOチャンネル作成完了しました"
        e.reply(message).setEphemeral(true).queue()
    }

    private fun scheduleCommand(e: SlashCommandInteractionEvent) {
        val dayString = e.getOption("day")?.asString ?: return
        val timeString = e.getOption("time")?.asString ?: return
        val channel = e.getOption("channel")?.asChannel ?: return
        val scenarioName = e.getOption("scenarioname")?.asString ?: return
        val channelID = channel.id
        val dateData = dateTimeManager.convertingDateTime("$dayString $timeString")

        // 日にち 時間が正しい形式か チェックする
        if (!dateTimeManager.isValidDateTime(dateData)) {
            val message = "日にち 時間の表記方法が間違っています"
            e.reply(message).setEphemeral(true).queue()
            return
        }
        val status = if (dateTimeManager.isWithinOneWeek(dateData)) {
            Data.NOTIFIED_WEEK_STATUS
        } else {
            Data.UN_NOTIFIED_STATUS
        } // 0:未設定 1:一週間前通知済み 2:一日前通知済み
        scheduleManager.make(scenarioName, dateData, channelID, status)

        val title = "日程登録"
        val color = Color.BLUE
        val fields = mutableListOf(
            MessageEmbed.Field("シナリオ名",scenarioName,false),
            MessageEmbed.Field("日程",dateData,false),
            MessageEmbed.Field("通知チャンネル","<#${channelID}>",false)
        )
        val embed = discordManager.makeEmbed(title = title,color = color, fields = fields)
        e.replyEmbeds(embed).setEphemeral(true).queue()
    }

    private fun deleteSchedule(e: SlashCommandInteractionEvent) {
        val id = e.getOption("id")?.asInt ?: return
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

    private fun checkSchedule(e:SlashCommandInteractionEvent) {
        fixedTermScheduleManager.checkSchedule()
        fixedTermScheduleManager.updateDateMessage()
        scheduleManager.autoDeleteOldSchedule()
        val message = "チェックを開始します"
        e.reply(message).setEphemeral(true).queue()
    }

    private fun send(e:SlashCommandInteractionEvent) {
        val message = "メッセージを送信します"
        val channel = e.channel
        val sendMessage = e.getOption("text")?.asString ?: return
        e.reply(message).setEphemeral(true).queue()
        channel.sendMessage(sendMessage).queue()
    }
}