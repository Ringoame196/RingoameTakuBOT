package com.github.ringoame196

import com.github.ringoame196.datas.Data
import com.github.ringoame196.manager.DiscordManager
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel
import java.awt.Color

class ScenarioStorageManager() {
    private val categoryID = Data.config.scenarioCategoryID
    private val discordManager = DiscordManager()

    private fun acquisitionScenarioStorage(): Map<String, List<ThreadChannel>> {
        return discordManager.acquisitionScenarioStorage(categoryID)
    }

    fun update() {
        val channelID = Data.config.scenarioSendChannelID ?: return
        val messageID = Data.config.scenarioSendMessageID ?: return

        val channel = Data.jda?.getTextChannelById(channelID) ?: return

        val title = ":placard: シナリオ情報"
        val fields = mutableListOf<MessageEmbed.Field>()

        val scenarioStorageList = acquisitionScenarioStorage()

        for ((formName,threatList) in scenarioStorageList) {
            fields.add(MessageEmbed.Field(formName,":book: ${threatList.size}シナリオ",false))
        }
        var embed = discordManager.makeEmbed(title,color= Color.GREEN, fields = fields)

        channel.editMessageById(messageID,"# シナリオ置き場情報").queue()
        channel.editMessageEmbedsById(messageID,embed).queue()
    }
}