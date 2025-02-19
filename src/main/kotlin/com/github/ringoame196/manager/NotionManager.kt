package com.github.ringoame196.manager

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.NotionScheduleData
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

class NotionManager {
    private val client = OkHttpClient()
    private val notionApiUrl = "https://api.notion.com/v1/databases/${Data.config.dataBaseID}/query"
    private val jsonMediaType = "application/json".toMediaType()

    fun acquisitionSchedule():List<NotionScheduleData> {
        val notionData = acquisitionNotionSchedule()
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
                val data = convertToDate(sessionDate)

                val channelId = properties.getAsJsonObject("チャンネルID")
                    ?.getAsJsonArray("rich_text")
                    ?.firstOrNull()
                    ?.asJsonObject
                    ?.get("plain_text")
                    ?.asString ?: "なし"

                val notification = properties.getAsJsonObject("通知")
                    ?.getAsJsonObject("select")  // 🔹 select 型に変更
                    ?.get("name")
                    ?.asString ?: "なし"

                NotionScheduleData(scenarioName, data, channelId,notification)
            } catch (e: Exception) {
                println("データの解析に失敗: ${e.message}")
                null
            }
        }
    }

    private fun convertToDate(datetime: String): Date {
        // 最初に日付形式（YYYY-MM-DD）を処理するためのフォーマット
        val formatDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            return formatDate.parse(datetime) // まずは日付だけの形式を試す
        } catch (e: Exception) {
            // 日付だけの形式で失敗した場合、ISO8601形式（日時＋タイムゾーン）を試す
            val formatDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
            return formatDateTime.parse(datetime) // 時間部分とタイムゾーンがある場合
        }
    }
}