package org.example

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import org.example.Events.CommandEvent
import org.example.Events.MessageReceivedEvent
import java.io.File

fun main() {
    var isStart = true
    val tokenFile = File("./token.txt")
    val activityFile = File("./activity.txt")

    if (!tokenFile.exists()) {
        tokenFile.writeText("")
        println("tokenファイルを生成しました")
        isStart = false
    }

    if (!activityFile.exists()) {
        activityFile.writeText("")
        println("activityファイルを生成しました")
        isStart = false
    }

    if (!isStart) return

    val token = tokenFile.readText() // tokenを取得
    val jdaBuilder = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT) // bot起動

    val activity = activityFile.readText() // アクティビティに表示する
    jdaBuilder.setActivity(Activity.playing(activity)) // アクティビティ設定

    // イベントリスナーを追加
    val jda = jdaBuilder.addEventListeners(CommandEvent(),MessageReceivedEvent()).build() // JDAオブジェクトを取得

    // スラッシュコマンドの登録
    jda.updateCommands().addCommands(
        Commands.slash("test", "テストコマンド"),
        Commands.slash("stop", "BOTをシャットダウンします"),
        Commands.slash("reset", "チャンネルをリセットする"),
        Commands.slash("makeho", "HOチャンネルを作成する").addOption(OptionType.STRING,"scenarioname","シナリオの名前",true).addOption(OptionType.NUMBER,"honumber","HOの数",true),
        Commands.slash("schedule", "スケジュール設定").addOption(OptionType.STRING,"day","予定日", true).addOption(OptionType.CHANNEL,"channel","通知チャンネル",true)
    ).queue()
}