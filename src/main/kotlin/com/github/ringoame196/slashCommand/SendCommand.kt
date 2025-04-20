package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class SendCommand: Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val message = "メッセージを送信します"
        val channel = e.channel
        val sendMessage = discordManager.acquisitionCommandOptions(e,SlashCommandConst.TEXT_OPTION)?.asString ?: return
        e.reply(message).setEphemeral(true).queue()
        channel.sendMessage(sendMessage).queue()
    }
}