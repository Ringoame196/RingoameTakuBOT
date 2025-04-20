package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class StopCommand: Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val jda = e.jda
        val message = "BOTの電源をオフにするよ！"

        e.reply(message).setEphemeral(true).queue({
            // メッセージが正常に送信された後にシャットダウンを実行
            discordManager.shutdown(jda)
        }, {
            it.printStackTrace()
        })
    }
}