package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.NotificationManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class CheckCommand: Command {
    private val notificationManager = NotificationManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val message = "チェックを走らせます"
        notificationManager.check()
        e.reply(message).setEphemeral(true).queue()
    }
}