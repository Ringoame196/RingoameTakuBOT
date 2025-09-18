package com.github.ringoame196.datas

import net.dv8tion.jda.api.JDA

object Data {
    // config
    lateinit var config: Config

    // Discord関係
    var jda: JDA? = null
    const val SESSION_VOICE_CHANNEL_ID = 1230148660881391779
}
