package org.example

data class ScheduleData(
    val id:Int,
    val scenarioName:String,
    val datetime:String,
    val channelId:String,
    val status:Int
    )