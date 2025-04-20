package com.github.ringoame196.slashCommand

import com.github.ringoame196.ScenarioStorageManager
import com.github.ringoame196.datas.Data
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class ArchiveCommand: Command {
    override fun runCommand(e: SlashCommandInteractionEvent) {
        val guild = e.guild ?: return
        val channel = e.channel.asTextChannel()
        val sessionVC = guild.getVoiceChannelById(Data.SESSION_VOICE_CHANNEL_ID)
        if (sessionVC == null) {
            val message = "❌️セッションVCが見つかりませんでした"
            e.reply(message).setEphemeral(true).queue()
            return
        }
        val members = sessionVC.members

        val message = "${members.size}人のロール設定します"
        e.reply(message).setEphemeral(true).queue()

        for (member in members) {
            channel.upsertPermissionOverride(member)
                .setAllowed(Permission.VIEW_CHANNEL)
                .queue()
        }
    }
}