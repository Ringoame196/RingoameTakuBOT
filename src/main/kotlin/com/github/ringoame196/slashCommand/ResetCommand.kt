package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.DiscordManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ResetCommand : Command {
    private val discordManager = DiscordManager()
    override fun runCommand(e: SlashCommandInteractionEvent) {
        val messageCountToLoad = 80
        val textChannel = e.channel

        // 最初に「メッセージを削除中です...」の非表示メッセージをセット
        e.deferReply(true).queue { hook ->
            CoroutineScope(Dispatchers.Default).launch {
                val messages = textChannel.history.retrievePast(messageCountToLoad).complete()
                val deletingMsg = "${messages.size}件のメッセージを削除中です..."
                hook.editOriginal(deletingMsg).queue()

                // メッセージ削除
                discordManager.deleteMessages(messages)

                // 完了メッセージに更新
                val doneMsg = "${messages.size}件のメッセージをリセットしました ✅"
                hook.editOriginal(doneMsg).queue()
            }
        }
    }
}
