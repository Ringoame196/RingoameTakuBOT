package com.github.ringoame196.manager

import com.github.ringoame196.event.MessageReceivedEvent
import com.github.ringoame196.event.SlashCommandConst
import com.github.ringoame196.event.SlashCommandInteraction
import com.github.ringoame196.datas.Data
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import java.awt.Color
import java.time.temporal.TemporalAccessor
import kotlin.system.exitProcess

class DiscordManager {


    fun shutdown(jda: JDA) {
        jda.shutdown() // シャットダウン
        exitProcess(0)
    }

    fun makeEmbed(title: String,titleURL:String? = null, color: Color? = null, descriptor: String? = null, image: String? = null, author: User? = null, footer: String? = null, thumbnail: String? = null, timestamp: TemporalAccessor? = null, fields: MutableList<MessageEmbed.Field>? = null): MessageEmbed {
        val embed = EmbedBuilder()
        embed.setTitle(title, titleURL) // タイトル
        embed.setDescription(descriptor) // 説明
        embed.setColor(color) // カラー
        embed.setImage(image) // 画像
        embed.setFooter(footer) // フッター
        embed.setThumbnail(thumbnail) // サムネ
        embed.setTimestamp(timestamp) // タイムスタンプ

        if (fields != null) { // フィールドを追加
            for (field in fields) {
                embed.addField(field)
            }
        }

        if (author != null) {
            val userName = author.name
            val iconURL = author.avatarUrl
            embed.setAuthor(userName, null, iconURL) // author
        }

        return embed.build()
    }

    fun setUpDiscordJDA():JDA {
        val token = Data.config.token
        val activity = Data.config.activity

        val jdaBuilder =
            JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT) // bot起動

        jdaBuilder.enableIntents(
            GatewayIntent.GUILD_VOICE_STATES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.SCHEDULED_EVENTS
        )
        if (activity != null) jdaBuilder.setActivity(Activity.playing(activity)) // アクティビティ設定

        // イベントリスナーを追加
        val jda =
            jdaBuilder.addEventListeners(SlashCommandInteraction(), MessageReceivedEvent()).build() // JDAオブジェクトを取得

        // スラッシュコマンドの登録
        jda.updateCommands().addCommands(
            Commands.slash(SlashCommandConst.TEST_COMMAND, "テストコマンド"),
            Commands.slash(SlashCommandConst.STOP_COMMAND, "BOTをシャットダウンします"),
            Commands.slash(SlashCommandConst.RESET_COMMAND, "チャンネルをリセットする"),
            Commands.slash(SlashCommandConst.MAKE_HO_COMMAND, "HOチャンネルを作成する")
                .addOption(OptionType.STRING, SlashCommandConst.SCENARIO_NAME_OPTION, "シナリオ名前", true)
                .addOption(OptionType.NUMBER, SlashCommandConst.HO_NUMBER_OPTION, "HO数", true),
            Commands.slash(SlashCommandConst.SCHEDULE_COMMAND, "スケジュール設定")
                .addOption(OptionType.STRING, SlashCommandConst.SCENARIO_NAME_OPTION, "シナリオ名", true)
                .addOption(OptionType.STRING, SlashCommandConst.DAY_OPTION, "予定日", true)
                .addOption(OptionType.STRING, SlashCommandConst.TIME_OPTION, "時間", true)
                .addOption(OptionType.CHANNEL, SlashCommandConst.CHANNEL_OPTION, "通知チャンネル", true)
                .addOption(OptionType.INTEGER, SlashCommandConst.STATUS_OPTION,"ステータス",true),
            Commands.slash(SlashCommandConst.DELETE_SCHEDULE_COMMAND, "スケジュールを削除する")
                .addOption(OptionType.INTEGER, SlashCommandConst.ID_OPTION, "セッション管理id", true),
            Commands.slash(SlashCommandConst.LIST_SCHEDULE_COMMAND, "スケジュールを確認する"),
            Commands.slash(SlashCommandConst.CHECK_SCHEDULE_COMMAND, "スケジュール通知のチェック"),
            Commands.slash(SlashCommandConst.SEND_COMMAND, "メッセージ送信")
                .addOption(OptionType.STRING, SlashCommandConst.TEXT_OPTION, "メッセージ", true),
            Commands.slash(SlashCommandConst.EDIT_SCHEDULE_COMMAND,"スケジュールの予定を編集")
                .addOption(OptionType.INTEGER, SlashCommandConst.ID_OPTION,"セッション管理id",true)
                .addOption(OptionType.STRING, SlashCommandConst.SCENARIO_NAME_OPTION, "シナリオ名", false)
                .addOption(OptionType.STRING, SlashCommandConst.DAY_OPTION, "予定日", false)
                .addOption(OptionType.STRING, SlashCommandConst.TIME_OPTION, "時間", false)
                .addOption(OptionType.CHANNEL, SlashCommandConst.CHANNEL_OPTION, "通知チャンネル", false)
                .addOption(OptionType.INTEGER, SlashCommandConst.STATUS_OPTION,"ステータス",false),
            ).queue()
        return jda
    }

    suspend fun deleteMessages(messages: MutableList<Message>) {
        for (message in messages) {
            if (!message.isPinned && (message.type == MessageType.DEFAULT || message.type == MessageType.INLINE_REPLY || message.type == MessageType.SLASH_COMMAND)) message.delete().queue()
            else if (message.reactions.isNotEmpty()) message.clearReactions().queue() // リアクションリセット
            delay(1000L) // 1秒遅延
        }
    }

    fun makeHOChannel(guild: Guild,scenarioName: String,hoNumber: Int) {
        guild.createCategory(scenarioName).queue { category ->
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
    }
}