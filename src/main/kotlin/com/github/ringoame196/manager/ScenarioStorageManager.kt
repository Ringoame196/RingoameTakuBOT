package com.github.ringoame196.manager

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScenarioStorage
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color

class ScenarioStorageManager() {
    private val categoryID = Data.config.scenarioCategoryID
    private val discordManager = DiscordManager()

    fun acquisitionScenarioStorageList(): List<ScenarioStorage> {
        return discordManager.acquisitionScenarioStorage(categoryID)
    }

    fun update(scenarioStorageList: List<ScenarioStorage>) {
        val channelID = Data.config.scenarioSendChannelID ?: return
        val messageID = Data.config.scenarioSendMessageID ?: return

        val channel = Data.jda?.getTextChannelById(channelID) ?: return

        val title = ":placard: シナリオ情報"
        val fields = mutableListOf<MessageEmbed.Field>()

        for (scenarioStorage in scenarioStorageList) {
            val formName = scenarioStorage.formName
            val threatList = scenarioStorage.threatList
            fields.add(MessageEmbed.Field(formName,":book: ${threatList.size}シナリオ",false))
        }
        var embed = discordManager.makeEmbed(title,color= Color.GREEN, fields = fields)

        channel.editMessageById(messageID,"# シナリオ置き場情報").queue()
        channel.editMessageEmbedsById(messageID,embed).queue()
    }
}