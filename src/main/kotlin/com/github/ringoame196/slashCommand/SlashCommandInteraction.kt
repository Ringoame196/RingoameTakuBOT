package com.github.ringoame196.slashCommand

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.FileUpload
import java.nio.charset.StandardCharsets

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
                SlashCommandConst.ARCHIVE_COMMAND -> ArchiveCommand()
                SlashCommandConst.ROLE_COMMAND -> RoleCommand()
                else -> return
            }
            command.runCommand(e)
        } catch (error: Exception) {
            val message = "エラーが発生しました\n以下エラー文です"
            val errorText = error.message ?: "エラー文がないようです"
            val fileName = "error.txt"
            val fileBytes = errorText.toByteArray(StandardCharsets.UTF_8)
            e.reply(message).addFiles(FileUpload.fromData(fileBytes, fileName)).setEphemeral(true).queue()
            println(error.message)
        }
    }

    private fun canUseCommand(member: Member): Boolean {
        return member.isOwner
    }
}
