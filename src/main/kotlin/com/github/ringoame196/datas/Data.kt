package com.github.ringoame196.datas

import net.dv8tion.jda.api.JDA

object Data {
    // データベース
    lateinit var dbFilePath: String
    const val TABLE_NAME = "schedule"
    const val ID_KEY = "id"
    const val SCENARIO_NAME_KEY = "scenario_name"
    const val DATE_KEY = "date_time"
    const val CHANNEL_ID_KEY = "channel_id"
    const val STATUS_KEY = "status"

    val CHECK_TIME: List<Int> = listOf(0,12)
    const val UN_NOTIFIED_STATUS = 0
    const val NOTIFIED_WEEK_STATUS = 1

    // config
    lateinit var config: Config

    // Discord関係
    var jda:JDA? = null
    const val ADMIN_ROLE_ID = "1252623868477050993"
    const val SESSION_VOICE_CHANNEL_URL = "https://discord.com/channels/1230147693834407996/1230148660881391779"
}