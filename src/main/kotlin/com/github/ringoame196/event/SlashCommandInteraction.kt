package com.github.ringoame196.event

import com.github.ringoame196.ScenarioStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import com.github.ringoame196.manager.DiscordManager
import com.github.ringoame196.datas.CommandOptions
import com.github.ringoame196.datas.Data
import com.github.ringoame196.manager.NotificationManager
import net.dv8tion.jda.api.hooks.ListenerAdapter


class SlashCommandInteraction : ListenerAdapter() {
    private val discordManager = DiscordManager()
    private val notificationManager = NotificationManager()

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

    private fun acquisitionCommandOptions(e:SlashCommandInteractionEvent): CommandOptions {
        return CommandOptions(
            e.getOption(SlashCommandConst.SCENARIO_NAME_OPTION)?.asString,
            e.getOption(SlashCommandConst.HO_NUMBER_OPTION)?.asDouble?.toInt(),
            e.getOption(SlashCommandConst.TEXT_OPTION)?.asString,
        )
    }

    private fun testCommand(e: SlashCommandInteractionEvent) {
        val message = "テストだよ！"
        e.reply(message).setEphemeral(true).queue()

        val scenarioStorageManager = ScenarioStorageManager()
        val scenarioStorageList = scenarioStorageManager.acquisitionScenarioStorageList()
        scenarioStorageManager.update(scenarioStorageList)
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

    private fun checkSchedule(e:SlashCommandInteractionEvent) {
        val message = "チェックを走らせます"
        notificationManager.check()
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
}