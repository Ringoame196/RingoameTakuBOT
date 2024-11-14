package org.example.Events

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.example.Managers.DiscordManager
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

        val diceList = List(numOfDice) { Random.nextInt(1, diceSides + 1) }
        val diceTotal = diceList.sum()

        val title = "ダイス結果"
        val dice = if (diceList.size == 1) "$diceTotal"
        else "$diceList → $diceTotal"
        val color = Color.GREEN
        val embed = discordManager.makeEmbed(title, descriptor = dice, color =  color, author = author)
        e.message.replyEmbeds(embed).queue()
    }
}