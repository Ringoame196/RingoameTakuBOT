package com.github.ringoame196.datas

import java.util.Date

data class NotionScheduleData(
    val scenarioName: String,
    val datetime: Date,
    val channelId: String,
    val status: String
)
