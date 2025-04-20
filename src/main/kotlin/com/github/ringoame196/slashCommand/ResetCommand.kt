package com.github.ringoame196.slashCommand

import com.github.ringoame196.ScenarioStorageManager
import com.github.ringoame196.manager.DiscordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ResetCommand: Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
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
}