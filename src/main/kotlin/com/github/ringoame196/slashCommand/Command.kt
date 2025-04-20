package com.github.ringoame196.slashCommand

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

interface Command {
    fun runCommand(e: SlashCommandInteractionEvent)
}