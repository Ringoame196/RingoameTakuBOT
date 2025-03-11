package com.github.ringoame196

import com.github.ringoame196.manager.ConfigManager
import com.github.ringoame196.manager.DiscordManager
import com.github.ringoame196.datas.Data
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

    val scenarioStorageManager = ScenarioStorageManager()
    scenarioStorageManager.update()

    executeRegularly() // 定期実行開始
}

fun executeRegularly() {
    // notion、スケジュール関係
    val notificationManager = NotificationManager()
    val scenarioStorageManager = ScenarioStorageManager()

    val scheduler = Executors.newSingleThreadScheduledExecutor()

    // 現在の時刻を取得
    val now = Calendar.getInstance()

    // 次回の0時を計算
    val midnight = Calendar.getInstance()
    midnight.set(Calendar.HOUR_OF_DAY, 0)
    midnight.set(Calendar.MINUTE, 0)
    midnight.set(Calendar.SECOND, 0)
    midnight.set(Calendar.MILLISECOND, 0)

    // 現在が0時を過ぎていれば、次の日の0時に設定
    if (now.after(midnight)) {
        midnight.add(Calendar.DAY_OF_MONTH, 1)
    }

    // 次回0時までの遅延時間をミリ秒単位で計算
    val delay = midnight.timeInMillis - now.timeInMillis

    // 次回0時にタスクを実行し、その後は毎日繰り返し
    scheduler.scheduleAtFixedRate({
        scenarioStorageManager.update()
        notificationManager.check()
    }, delay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS)
}