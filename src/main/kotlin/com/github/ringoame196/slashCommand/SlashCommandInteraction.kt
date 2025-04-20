package com.github.ringoame196.slashCommand

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class SlashCommandInteraction : ListenerAdapter() {

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val member = e.member ?: return
        if (!canUseCommand(member)) {
            val message = "あなたは、権限を持っていません"
            e.reply(message).queue()
            return
        }

        try {
            val command: Command = when (e.name) {
                SlashCommandConst.TEST_COMMAND -> TestCommand()
                SlashCommandConst.STOP_COMMAND -> StopCommand()
                SlashCommandConst.RESET_COMMAND -> ResetCommand()
                SlashCommandConst.MAKE_HO_COMMAND -> MakeHOCommand()
                SlashCommandConst.CHECK_SCHEDULE_COMMAND -> CheckCommand()
                SlashCommandConst.SEND_COMMAND -> SendCommand()
                SlashCommandConst.JOIN_VC_COMMAND -> JoinVCCommand()
                else -> return
            }
            command.runCommand(e)
        } catch (error:Exception) {
            val message = "エラーが発生しました"
            e.reply(message).setEphemeral(true).queue()
            println(error.message)
        }
    }

    private fun canUseCommand(member: Member):Boolean {
        return member.isOwner
    }
}