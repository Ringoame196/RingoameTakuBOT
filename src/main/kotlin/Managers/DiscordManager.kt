package org.example.Managers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import java.awt.Color
import java.time.temporal.TemporalAccessor

class DiscordManager {
    fun shutdown(jda: JDA) {
        jda.shutdown() // シャットダウン
    }

    fun makeEmbed(title: String,titleURL:String? = null, color: Color? = null, descriptor: String? = null, image: String? = null, author: User? = null, footer: String? = null, thumbnail: String? = null, timestamp: TemporalAccessor? = null, fields: MutableList<MessageEmbed.Field>? = null): MessageEmbed {
        val embed = EmbedBuilder()
        embed.setTitle(title, titleURL) // タイトル
        embed.setDescription(descriptor) // 説明
        embed.setColor(color) // カラー
        embed.setImage(image) // 画像
        embed.setFooter(footer) // フッター
        embed.setThumbnail(thumbnail) // サムネ
        embed.setTimestamp(timestamp) // タイムスタンプ

        if (fields != null) { // フィールドを追加
            for (field in fields) {
                embed.addField(field)
            }
        }

        if (author != null) {
            val userName = author.name
            val iconURL = author.avatarUrl
            embed.setAuthor(userName, null, iconURL) // author
        }

        return embed.build()
    }
}