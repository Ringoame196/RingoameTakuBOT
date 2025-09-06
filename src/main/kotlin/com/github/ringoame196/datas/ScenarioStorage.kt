package com.github.ringoame196.datas

import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel

data class ScenarioStorage(
    val formName: String,
    val threatList: List<ThreadChannel>
)
