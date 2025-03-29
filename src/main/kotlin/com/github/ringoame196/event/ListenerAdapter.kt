package com.github.ringoame196.event

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter


class ListenerAdapter : ListenerAdapter() {

    override fun onSlashCommandInteraction(e: SlashCommandInteractionEvent) {
        val slashCommandInteraction = SlashCommandInteraction()
        slashCommandInteraction.onSlashCommandInteraction(e)
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        val messageReceivedEvent = MessageReceivedEvent()
        messageReceivedEvent.onMessageReceived(e)
    }

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        val guildMemberJoinEvent = GuildMemberJoinEvent()
        guildMemberJoinEvent.onGuildMemberJoin(e)
    }
}