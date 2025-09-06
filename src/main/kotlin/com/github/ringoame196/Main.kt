package com.github.ringoame196

import com.github.ringoame196.datas.Data
import com.github.ringoame196.manager.ConfigManager
import com.github.ringoame196.manager.DiscordManager
import com.github.ringoame196.manager.NotificationManager
import java.util.Calendar
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

    executeRegularly() // 定期実行開始
}

fun executeRegularly() {
    val notificationManager = NotificationManager()
    val scheduler = Executors.newSingleThreadScheduledExecutor()
    val now = Calendar.getInstance()

    // ===== 0時タスク =====
    val midnight = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (now.after(this)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val delayMidnight = midnight.timeInMillis - now.timeInMillis

    scheduler.scheduleAtFixedRate({
        notificationManager.check()
    }, delayMidnight, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)

    // ===== 1時タスク =====
    val discordManager = DiscordManager()
    val oneAM = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 1)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (now.after(this)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val delayOneAM = oneAM.timeInMillis - now.timeInMillis

    scheduler.scheduleAtFixedRate({
        val jda = Data.jda
        if (jda != null) {
            println("再起動しました")
            discordManager.shutdown(jda)
        } else {
            println("再起動に失敗しました")
        }
    }, delayOneAM, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)
}
