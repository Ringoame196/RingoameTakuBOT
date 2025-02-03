package com.github.ringoame196.Managers

import com.github.ringoame196.datas.Config
import com.github.ringoame196.datas.Data
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ConfigManager {
    private val yamlManager = YamlFileManager()
    private val configPath = "config.yml"

    fun exists(): Boolean {
        return !File("./$configPath").exists()
    }

    fun canStart(): Boolean {
        return Data.config.token != null
    }

    fun acquisitionData() {
        val fileData = yamlManager.loadYAsMap(configPath)
        val config = Config(
            fileData["token"],
            fileData["activity"],
            fileData["date_message_id"],
            fileData["date_channel_id"]
        )
        Data.config = config
    }

    fun make() {
        val currentPath = File(".").canonicalPath
        val targetFile = File("$currentPath/$configPath")
        val resourceStream: InputStream? = object {}.javaClass.getResourceAsStream("/config.yml")
        if (resourceStream == null) {
            println("リソースファイルが見つかりません: config.yml")
            return
        }
        Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        println("設定ファイルの生成完了")
    }
}