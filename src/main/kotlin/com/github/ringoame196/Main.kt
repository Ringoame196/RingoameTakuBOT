package com.github.ringoame196

import com.github.ringoame196.Managers.DataBaseManager
import com.github.ringoame196.Managers.DiscordManager
import com.github.ringoame196.Managers.FixedTermScheduleManager
import com.github.ringoame196.Managers.ScheduleManager
import com.github.ringoame196.datas.Data
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
    val currentPath = File(".").canonicalPath
    val dbFilePath = "${currentPath}\\data.db"
    val dataBaseManager = DataBaseManager()
    Data.dbFilePath = dbFilePath
    dataBaseManager.makeDataBase()

    val discordManager = DiscordManager()
    val token = fileData["token"] ?:return // tokenを取得
    val activity = fileData["activity"] ?:return // アクティビティに表示する
    Data.dateMessageID = fileData["date_message_id"] ?:return // dateMessageID
    Data.dateChannelID = fileData["date_channel_id"] ?:return // dateChannelID
    val jda = discordManager.setUpDiscordJDA(token, activity)

    jda.awaitReady()
    Data.jda = jda

    val scheduleManager = ScheduleManager()
    val fixedTermScheduleManager = FixedTermScheduleManager()
    scheduleManager.autoDeleteOldSchedule() // 古いschedule削除
    fixedTermScheduleManager.startFixedTermCheck() // 定期スケジュールチェック開始
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