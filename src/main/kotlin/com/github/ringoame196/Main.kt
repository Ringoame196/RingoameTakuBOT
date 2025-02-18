package com.github.ringoame196

import com.github.ringoame196.managers.ConfigManager
import com.github.ringoame196.managers.DataBaseManager
import com.github.ringoame196.managers.DiscordManager
import com.github.ringoame196.managers.FixedTermScheduleManager
import com.github.ringoame196.managers.ScheduleManager
import com.github.ringoame196.datas.Data
import java.io.File

fun main() {
    val configManager = ConfigManager()

    if (!configManager.exists()) configManager.make()
    configManager.acquisitionData()

    if (!configManager.canStart()) {
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
    val jda = discordManager.setUpDiscordJDA()

    jda.awaitReady()
    Data.jda = jda

    val scheduleManager = ScheduleManager()
    val fixedTermScheduleManager = FixedTermScheduleManager()
    scheduleManager.autoDeleteOldSchedule() // 古いschedule削除
    fixedTermScheduleManager.startFixedTermCheck() // 定期スケジュールチェック開始
}