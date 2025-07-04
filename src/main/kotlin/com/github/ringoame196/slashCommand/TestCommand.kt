package com.github.ringoame196.slashCommand

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class TestCommand: Command {
    override fun runCommand(e: SlashCommandInteractionEvent) {
        val message = "テストだよ！"
        e.reply(message).setEphemeral(true).queue()
    }
}