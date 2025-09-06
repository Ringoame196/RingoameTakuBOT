package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class MakeHOCommand : Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild ?: return
        val scenarioName = discordManager.acquisitionCommandOptions(e, SlashCommandConst.SCENARIO_NAME_OPTION)?.asString ?: return
        val hoNumber = discordManager.acquisitionCommandOptions(e, SlashCommandConst.HO_NUMBER_OPTION)?.asInt ?: return

        discordManager.makeHOChannel(
            guild,
            scenarioName,
            hoNumber,
            onFailure = { error ->
                e.reply("チャンネル作成中にエラーが発生しました: ${error.message}")
                    .setEphemeral(true)
                    .queue()
            }
        )

        e.reply("HOチャンネル作成を開始しました").setEphemeral(true).queue()
    }
}
