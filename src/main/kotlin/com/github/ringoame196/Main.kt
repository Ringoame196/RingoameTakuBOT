package com.github.ringoame196

import com.github.ringoame196.manager.ConfigManager
import com.github.ringoame196.manager.DiscordManager
import com.github.ringoame196.datas.Data
import com.github.ringoame196.manager.NotificationManager

fun main() {
    // config関係
    val configManager = ConfigManager()

    if (!configManager.exists()) configManager.make()
    configManager.acquisitionData()

    if (!configManager.canStart()) {
        println("必要ファイルが足りないため 起動を停止します")
        return
    }

    // discord関係
    val discordManager = DiscordManager()
    val jda = discordManager.setUpDiscordJDA()

    jda.awaitReady()
    Data.jda = jda

    // notion、スケジュール関係
    val notificationManager = NotificationManager()
    notificationManager.scheduleDailyTaskAtMidnight()
}