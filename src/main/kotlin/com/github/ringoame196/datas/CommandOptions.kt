package com.github.ringoame196.datas

import net.dv8tion.jda.api.entities.channel.Channel

data class CommandOptions(
    val scenarioName:String?,
    val hoNumber:Int?,
    val day:String?,
    val time:String?,
    val channel: Channel?,
    val id:Int?,
    val text:String?,
    val status:Int?
)
