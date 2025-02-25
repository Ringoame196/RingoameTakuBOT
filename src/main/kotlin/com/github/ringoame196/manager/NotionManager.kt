package com.github.ringoame196.manager

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.NotionScheduleData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class NotionManager {
    private val client = OkHttpClient()
    private val notionApiUrl = "https://api.notion.com/v1/databases/${Data.config.dataBaseID}/query"
    private val jsonMediaType = "application/json".toMediaType()

    fun acquisitionSchedule():List<NotionScheduleData> {
        val notionData = acquisitionNotionSchedule()
        // コンソールメッセージ
        val message = "${notionData.size}件のスケジュールを取得しました"
        println(message)
        return conversionNotionScheduleData(notionData)
    }

    // 🔹 Notion API から今日以降のデータを取得
    private fun acquisitionNotionSchedule(): List<JsonObject> {
        val allResults = mutableListOf<JsonObject>()
        var nextCursor: String? = null
        val today = LocalDate.now().toString() // 今日の日付 (YYYY-MM-DD)

        do {
            val jsonBody = if (nextCursor == null) {
                """
    {
        "filter": {
            "and": [
                {
                    "property": "セッション日",
                    "date": {
                        "on_or_after": "$today"
                    }
                },
                {
                    "or": [
                        {
                            "property": "通知",
                            "select": {
                                "equals": "${Data.NOTIFICATION_ONE_DAYS_AGO}"
                            }
                        },
                        {
                            "property": "通知",
                            "select": {
                                "equals": "${Data.NOTIFICATION_SEVEN_DAYS_AGO}"
                            }
                        }
                    ]
                }
            ]
        },
        "page_size": 100
    }
    """.trimIndent()
            } else {
                """
    {
        "filter": {
            "and": [
                {
                    "property": "セッション日",
                    "date": {
                        "on_or_after": "$today"
                    }
                },
                {
                    "or": [
                        {
                            "property": "通知",
                            "select": {
                                "equals": "${Data.NOTIFICATION_ONE_DAYS_AGO}"
                            }
                        },
                        {
                            "property": "通知",
                            "select": {
                                "equals": "${Data.NOTIFICATION_SEVEN_DAYS_AGO}"
                            }
                        }
                    ]
                }
            ]
        },
        "page_size": 100,
        "start_cursor": "$nextCursor"
    }
    """.trimIndent()
            }


            val requestBody = jsonBody.toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url(notionApiUrl)
                .addHeader("Authorization", "Bearer ${Data.config.notionAPIKey}")
                .addHeader("Notion-Version", "2022-06-28")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    val jsonObject = JsonParser.parseString(responseBody).asJsonObject
                    val results = jsonObject.getAsJsonArray("results")

                    results.forEach { allResults.add(it.asJsonObject) }

                    nextCursor = jsonObject.get("next_cursor")?.takeIf { !it.isJsonNull }?.asString
                } else {
                    println("Error: ${response.code} - $responseBody")
                    return emptyList()
                }
            }
        } while (nextCursor != null)

        return allResults
    }

    // 🔹 Notion のデータを変換
    private fun conversionNotionScheduleData(data: List<JsonObject>): List<NotionScheduleData> {
        return data.mapNotNull { obj ->
            try {
                val properties = obj.getAsJsonObject("properties")

                val scenarioName = properties.getAsJsonObject("シナリオ名")
                    ?.getAsJsonArray("title")
                    ?.firstOrNull()
                    ?.asJsonObject
                    ?.get("plain_text")
                    ?.asString ?: "不明"

                val sessionDate = properties.getAsJsonObject("セッション日")
                    ?.getAsJsonObject("date")
                    ?.get("start")
                    ?.asString ?: "未定"

                val data = parseISO8601(sessionDate)

                if (data != null) {
                    val channelId = properties.getAsJsonObject("チャンネルID")
                        ?.getAsJsonArray("rich_text")
                        ?.firstOrNull()
                        ?.asJsonObject
                        ?.get("plain_text")
                        ?.asString ?: "なし"

                    NotionScheduleData(scenarioName, data, channelId)
                } else {
                    println("日にちがnullです")
                    null
                }

            } catch (e: Exception) {
                println("データの解析に失敗: ${e.message}")
                null
            }
        }
    }

    fun parseISO8601(isoString: String): Date? {
        return try {
            // ISO 8601 のフォーマットで解析
            val zonedDateTime = ZonedDateTime.parse(isoString, DateTimeFormatter.ISO_DATE_TIME)

            return Date.from(zonedDateTime.toInstant())
        } catch (e: Exception) {
            println("日付の解析に失敗: ${e.message}")
            null
        }
    }
}