package org.example.Managers

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import org.example.EmbedData

class DiscordManager {
    fun shutdown(jda: JDA) {
        jda.shutdown() // シャットダウン
    }

    fun sendEmbed(channel: MessageChannelUnion, embedData: EmbedData){
        // embedを作成
        val embedBuilder = EmbedBuilder()
        if (embedData.title != null) embedBuilder.setTitle(embedData.title)
        if (embedData.description != null) embedBuilder.setDescription(embedData.description)
        embedBuilder.setColor(embedData.color)
        if (embedData.fields != null) {
            // フィールドを追加
            embedData.fields?.forEach { (name, value) ->
                embedBuilder.addField(name, value, false)
            }
        }
        val embed = embedBuilder.build()
        channel.sendMessageEmbeds(embed).queue() // embedを送信
    }
}