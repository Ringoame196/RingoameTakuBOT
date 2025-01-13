package com.github.ringoame196.Events

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import com.github.ringoame196.Managers.DiscordManager
import java.awt.Color
import kotlin.random.Random

class MessageReceivedEvent: ListenerAdapter() {
    private val discordManager = DiscordManager()

    override fun onMessageReceived(e: MessageReceivedEvent) {
        val message = e.message.contentRaw
        val author = e.author
        val dicePattern = Regex("^!(\\d+)d(\\d+)$") // 正規表現パターン：サイコロの数と面数をキャプチャ

        val matchResult = dicePattern.find(message) ?: return

        val numOfDice = matchResult.groupValues[1].toInt()  // 第一キャプチャグループ: サイコロの数
        val diceSides = matchResult.groupValues[2].toInt()  // 第二キャプチャグループ: サイコロの面数

        try {
            val diceList = List(numOfDice) { Random.nextInt(1, diceSides + 1) }
            val diceTotal = diceList.sum()

            val title = "ダイス結果"
            val dice = if (diceList.size == 1) "$diceTotal"
            else "$diceList → $diceTotal"
            val descriptor = "$message -> $dice"
            val color = Color.GREEN
            val embed = discordManager.makeEmbed(title, descriptor = descriptor, color =  color, author = author)
            e.message.replyEmbeds(embed).queue()
        } catch (error:IllegalArgumentException) {
            val title = "エラー"
            val color = Color.RED
            val descriptor = "ダイス目が大きすぎるため処理できませんでした"
            val embed = discordManager.makeEmbed(title = title,color = color, descriptor = descriptor)
            e.message.replyEmbeds(embed).queue()
        }
    }
}