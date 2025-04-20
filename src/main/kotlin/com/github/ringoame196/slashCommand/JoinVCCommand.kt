package com.github.ringoame196.slashCommand

import com.github.ringoame196.ScenarioStorageManager
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

class JoinVCCommand: Command {
    override fun runCommand(e: SlashCommandInteractionEvent) {
        val member = e.member ?: return
        val guild = e.guild ?: return

        val voiceState = member.voiceState
        val channel = voiceState?.channel

        if (channel == null) {
            val message = "❌VCに参加していません"
            e.reply(message).setEphemeral(true).queue()
            return
        }

        val audioManager = guild.audioManager
        audioManager.openAudioConnection(channel)
        e.reply("✅VC に接続しました").queue()
    }
}