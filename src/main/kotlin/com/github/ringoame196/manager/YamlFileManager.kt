package com.github.ringoame196.manager

import org.yaml.snakeyaml.Yaml
import java.io.File

class YamlFileManager {
    fun loadYAsMap(filePath: String): Map<String, String> {
        val yaml = Yaml()
        val inputStream = File(filePath).inputStream()
        return yaml.load(inputStream) as Map<String, String>
    }
}
