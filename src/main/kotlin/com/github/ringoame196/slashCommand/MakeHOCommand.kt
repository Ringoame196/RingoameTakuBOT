package com.github.ringoame196.slashCommand

import com.github.ringoame196.ScenarioStorageManager
import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MakeHOCommand: Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild ?: return
        val scenarioName = discordManager.acquisitionCommandOptions(e,SlashCommandConst.SCENARIO_NAME_OPTION)?.asString ?: return
        val hoNumber = discordManager.acquisitionCommandOptions(e,SlashCommandConst.HO_NUMBER_OPTION)?.asInt ?: return

        discordManager.makeHOChannel(guild,scenarioName,hoNumber)

        val message = "HOチャンネル作成完了しました"
        e.reply(message).setEphemeral(true).queue()
    }
}