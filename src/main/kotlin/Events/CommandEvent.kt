package org.example.Events

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.example.Managers.DiscordManager
import org.example.Managers.ScheduleManager
import java.awt.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException



class CommandEvent : ListenerAdapter() {
    private val discordManager = DiscordManager()
    private val scheduleManager = ScheduleManager()

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {

        when (e.name) {
            "test" -> testCommand(e)
            "stop" -> stopCommand(e)
            "reset" -> resetCommand(e)
            "makeho" -> makeHOCommand(e)
            "schedule" -> scheduleCommand(e)
            "deleteschedule" -> deleteSchedule(e)
            "listschedule" -> listSchedule(e)
            else -> e.reply("不明なコマンドです。").queue()
        }
    }

    private fun testCommand(e: SlashCommandInteractionEvent) {
        val message = "テストだよ！"
        e.reply(message).queue()
    }

    private fun stopCommand(e: SlashCommandInteractionEvent) {
        val member = e.member // 送信者
        val jda = e.jda
        val message = "BOTの電源をオフにするよ！"

        if (member?.isOwner == true) { // 鯖の所有者のみ実行可能にする
            e.reply(message).queue()
            discordManager.shutdown(jda)
        }
    }

    private fun resetCommand(e: SlashCommandInteractionEvent) {
        val member = e.member // 送信者
        val messageCountToLoad = 80 // 取得するメッセージ数
        val textChannel = e.channel // テキストチャンネル
        if (member?.hasPermission(Permission.MESSAGE_MANAGE) == true) {
            // コルーチンを使用して非同期処理を実行
            CoroutineScope(Dispatchers.Default).launch {
                val messages = textChannel.history.retrievePast(messageCountToLoad).complete() // 指定数のメッセージを取得
                var deleteCount = 0 // メッセージを削除した回数
                var clearReactionCount = 0 // リアクションをクリアした回数

                val startMessage = "チャンネルリセット開始します"
                e.reply(startMessage).queue()

                for (message in messages) {
                    // ここでメッセージの内容を表示または処理
                    if (!message.isPinned && (message.type == MessageType.DEFAULT || message.type == MessageType.INLINE_REPLY || message.type == MessageType.SLASH_COMMAND)) {
                        message.delete().queue()
                        delay(1000L) // 1秒遅延
                        deleteCount++
                    } else if (message.reactions.size > 0) {
                        message.clearReactions().queue() // リアクションリセット
                        clearReactionCount++
                    }
                }
                sendEndMessage(textChannel, deleteCount, clearReactionCount) // 終了メッセージを送信
            }
        }
    }

    private fun sendEndMessage(textChannel: MessageChannelUnion, deleteCount:Int, clearReactionCount:Int) {
        val result = mutableListOf(
            MessageEmbed.Field("メッセージ削除数","${deleteCount}個",false),
            MessageEmbed.Field("リアクション削除数","${clearReactionCount}個",false)
        )

        val embed = discordManager.makeEmbed(title = "[リセット]", descriptor =  "「${textChannel.name}」チャンネルのリセット", color = Color.red,fields = result)
        discordManager.sendEmbed(textChannel,embed)
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
        e.reply(message).queue()
    }

    private fun scheduleCommand(e: SlashCommandInteractionEvent) {
        val dayString = e.getOption("day")?.asString ?: return
        val timeString = e.getOption("time")?.asString ?: return
        val channel = e.getOption("channel")?.asChannel ?: return
        val scenarioName = e.getOption("scenarioname")?.asString ?: return
        val channelID = channel.id
        val dateData = "${dayString}T${timeString}"
        val status = 0 // 0:未設定 1:一週間前通知済み 2:一日前通知済み

        scheduleManager.make(scenarioName, dateData, channelID, status)

        // 日にち 時間が正しい形式か チェックする
        if (!isValidDateTime(dateData)) {
            val message = "日にち 時間の表記方法が間違っています"
            e.reply(message).queue()
            return
        }



        val title = "日程登録"
        val color = Color.BLUE
        val fields = mutableListOf(
            MessageEmbed.Field("シナリオ名",scenarioName,false),
            MessageEmbed.Field("日程",dateData,false),
            MessageEmbed.Field("通知チャンネル","<#${channelID}>",false)
        )
        val embed = discordManager.makeEmbed(title = title,color = color, fields = fields)
        e.replyEmbeds(embed).queue()
    }

    private fun isValidDateTime(dateTime: String): Boolean {
        try {
            // LocalDateTimeの標準フォーマットで解析
            LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            return true
        } catch (e: DateTimeParseException) {
            return false
        }
    }

    private fun deleteSchedule(e: SlashCommandInteractionEvent) {
        val id = e.getOption("id")?.asInt ?: return
        scheduleManager.delete(id)
        val title = "スケジュール削除"
        val descriptor = "${id}のスケジュールを削除しました"
        val color = Color.RED

        val embed = discordManager.makeEmbed(title = title, descriptor = descriptor,color = color)
        e.replyEmbeds(embed).queue()
    }

    private fun listSchedule(e:SlashCommandInteractionEvent) {
        scheduleManager.sendAllSchedule(e)
    }
}