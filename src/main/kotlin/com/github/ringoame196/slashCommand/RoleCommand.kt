package com.github.ringoame196.slashCommand

import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class RoleCommand: Command {
    private val discordManager = DiscordManager()

    override fun runCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild
        val user = discordManager.acquisitionCommandOptions(e,SlashCommandConst.USER_OPTION)?.asUser ?: return
        val role = discordManager.acquisitionCommandOptions(e, SlashCommandConst.ROLE_OPTION)?.asRole ?: return
        guild!!.addRoleToMember(user, role).queue(
            { e.reply("${user.name} に${role.name}を付与しました").setEphemeral(true).queue() },
            { error -> e.reply("エラー: ${error.message}").setEphemeral(true).queue() }
        )
    }
}