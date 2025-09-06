package com.github.ringoame196.datas

import net.dv8tion.jda.api.JDA

object Data {
    // config
    lateinit var config: Config

    // Discord関係
    var jda: JDA? = null
    const val SESSION_VOICE_CHANNEL_URL = "https://discord.com/channels/1230147693834407996/1230148660881391779"
    const val SESSION_VOICE_CHANNEL_ID = 1230148660881391779

    // notion用
    const val NOTIFICATION_ONE_DAYS_AGO = "1日前のみ"
    const val NOTIFICATION_SEVEN_DAYS_AGO = "通知あり"
}
