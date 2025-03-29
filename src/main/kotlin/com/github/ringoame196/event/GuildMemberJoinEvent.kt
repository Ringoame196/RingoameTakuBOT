package com.github.ringoame196.event

import com.github.ringoame196.datas.Data
import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class GuildMemberJoinEvent: ListenerAdapter() {
    private val discordManager = DiscordManager()

    override fun onGuildMemberJoin(e: GuildMemberJoinEvent) {
        val welcomeChannelID = Data.config.welcomeChannelID ?: return
        val welcomeChannel = e.guild.getTextChannelById(welcomeChannelID) ?: return
        val user = e.user
        if (user.isBot) return // BOTだった場合は 通知しない

        val title = "Welcome"
        val color = Color.GREEN
        val embedMessage =
            "${user.asMention} さん、りんご飴卓へようこそ！🎉\n" +
                    "まずは <#1253646276424372315> チャンネルにある\n" +
                    "「ルール」に目を通し、同意をお願いします。\n" +
                    "セッションに必要なロールは、サーバー管理者が付与します。\n" +
                    "同意後、しばらくお待ちください。"

        val bot = e.jda.selfUser

        val message = "${user.asMention}さん ようこそ！！"
        val embed = discordManager.makeEmbed(title = title,color = color, descriptor = embedMessage, author = bot)

        welcomeChannel.sendMessage(message).setEmbeds(embed).queue()
    }
}