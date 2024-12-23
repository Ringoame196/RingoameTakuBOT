package org.example.datas

import net.dv8tion.jda.api.JDA

object Data {
    var jda:JDA? = null

    var dbFilePath:String? = null
    const val TABLE_NAME = "schedule"
    const val ID_KEY = "id"
    const val SCENARIO_NAME_KEY = "scenario_name"
    const val DATE_KEY = "date_time"
    const val CHANNEL_ID_KEY = "channel_id"
    const val STATUS_KEY = "status"

    const val UN_NOTIFIED_STATUS = 0
    const val NOTIFIED_WEEK_STATUS = 1

    var dateMessageID:String? = null
    var dateChannelID: String? = null
}