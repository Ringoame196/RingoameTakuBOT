package org.example

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import org.example.Events.SlashCommandInteraction
import org.example.Events.MessageReceivedEvent
import org.example.Events.SlashCommandConst
import org.example.Managers.DataBaseManager
import org.example.Managers.ScheduleManager
import org.example.datas.Data
import java.io.File

fun main() {
    val tokenFile = File("./token.txt")
    val activityFile = File("./activity.txt")
    val dateChannelIDFile = File("./date_channel_id.txt")
    val dateMessageIDFile = File("./date_message_id.txt")
    var isStart = tokenFile.exists() && activityFile.exists()
    val fileList = mutableListOf(
        tokenFile,
        activityFile,
        dateChannelIDFile,
        dateMessageIDFile
    )

    makeFile(fileList) // 必要ファイルを生成
    val fileData = acquisitionFileData(fileList)

    if (!isStart) {
        println("必要ファイルが足りないため 起動を停止します")
        return
    }

    // db作成
    makeDataBase()

    val token = fileData["token"] ?:return // tokenを取得
    val activity = fileData["activity"] ?:return // アクティビティに表示する
    Data.dateMessageID = fileData["date_message_id"] ?:return // dateMessageID
    Data.dateChannelID = fileData["date_channel_id"] ?:return // dateChannelID
    val jda = setUpDiscordJDA(token, activity)

    jda.awaitReady()
    Data.jda = jda

    val scheduleManager = ScheduleManager()
    scheduleManager.autoDeleteOldSchedule()

    scheduleManager.startFixedTermCheck() // 定期スケジュールチェック開始
}

private fun makeFile(fileList: List<File>) {
    for (file in fileList) {
        if (!file.exists()) {
            file.writeText("")
            println("[生成]${file.name}")
        }
    }
}

private fun acquisitionFileData(fileList: List<File>): MutableMap<String, String?> {
    val data = mutableMapOf<String, String?>()

    for (file in fileList) {
        val key = file.name.replace(".txt","")
        val text = file.readText()
        data[key] = text
    }
    return data
}

private fun makeDataBase() {
    val currentPath = File(".").canonicalPath
    val dbFilePath = "${currentPath}\\data.db"
    val dataBaseManager = DataBaseManager(currentPath)
    Data.dbFilePath = dbFilePath
    if (!File(dbFilePath).exists()) {
        val scheduleCommand =
            "CREATE TABLE IF NOT EXISTS ${Data.TABLE_NAME} (${Data.ID_KEY} INTEGER PRIMARY KEY AUTOINCREMENT, ${Data.SCENARIO_NAME_KEY} TEXT NOT NULL, ${Data.DATE_KEY} DATETIME NOT NULL, ${Data.CHANNEL_ID_KEY} TEXT NOT NULL, ${Data.STATUS_KEY} INTEGER NOT NULL);"
        dataBaseManager.executeUpdate(scheduleCommand)
    }
}

private fun setUpDiscordJDA(token:String,activity:String):JDA {
    val jdaBuilder = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT) // bot起動

    jdaBuilder.setActivity(Activity.playing(activity)) // アクティビティ設定

    // イベントリスナーを追加
    val jda = jdaBuilder.addEventListeners(SlashCommandInteraction(),MessageReceivedEvent()).build() // JDAオブジェクトを取得

    // スラッシュコマンドの登録
    jda.updateCommands().addCommands(
        Commands.slash(SlashCommandConst.TEST_COMMAND, "テストコマンド"),
        Commands.slash(SlashCommandConst.STOP_COMMAND, "BOTをシャットダウンします"),
        Commands.slash(SlashCommandConst.RESET_COMMAND, "チャンネルをリセットする"),
        Commands.slash(SlashCommandConst.MAKE_HO_COMMAND, "HOチャンネルを作成する").addOption(OptionType.STRING,"scenarioname","シナリオの名前",true).addOption(OptionType.NUMBER,"honumber","HOの数",true),
        Commands.slash(SlashCommandConst.SCHEDULE_COMMAND, "スケジュール設定").addOption(OptionType.STRING,"scenarioname","シナリオ名",true).addOption(OptionType.STRING,"day","予定日", true).addOption(OptionType.STRING,"time","時間",true).addOption(OptionType.CHANNEL,"channel","通知チャンネル",true),
        Commands.slash(SlashCommandConst.DELETE_SCHEDULE_COMMAND,"スケジュールを削除する").addOption(OptionType.INTEGER,"id","セッション管理id",true),
        Commands.slash(SlashCommandConst.LIST_SCHEDULE_COMMAND,"スケジュールを確認する"),
        Commands.slash(SlashCommandConst.CHECK_SCHEDULE_COMMAND, "スケジュール通知のチェック"),
        Commands.slash(SlashCommandConst.SEND_COMMAND,"メッセージ送信").addOption(OptionType.STRING,"text","メッセージ",true)

    ).queue()
    return jda
}