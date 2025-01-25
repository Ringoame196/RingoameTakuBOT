package com.github.ringoame196.Managers

import com.github.ringoame196.datas.Data
import com.github.ringoame196.datas.ScheduleData
import java.io.File
import java.sql.*

class DataBaseManager() {
    /**
     * SQLコマンドを実行する
     * @param command 実行するSQL文
     * @param parameters パラメータリスト
     */

    fun makeDataBase() {
        if (!File(Data.dbFilePath).exists()) {
            val scheduleCommand =
                "CREATE TABLE IF NOT EXISTS ${Data.TABLE_NAME} (${Data.ID_KEY} INTEGER PRIMARY KEY AUTOINCREMENT, ${Data.SCENARIO_NAME_KEY} TEXT NOT NULL, ${Data.DATE_KEY} DATETIME NOT NULL, ${Data.CHANNEL_ID_KEY} TEXT NOT NULL, ${Data.STATUS_KEY} INTEGER NOT NULL);"
            executeUpdate(scheduleCommand)
        }
    }

    fun executeUpdate(command: String, parameters: List<Any>? = null) {
        try {
            connection.use { conn ->
                conn.prepareStatement(command).use { preparedStatement ->
                    parameters?.bindParameters(preparedStatement)
                    preparedStatement.executeUpdate()
                }
            }
        } catch (e: SQLException) {
            println("SQL Error: ${e.message}")
            throw e
        }
    }

    /**
     * 単一の値を取得する
     * @param sql 実行するSQL文
     * @param parameters パラメータリスト
     * @param label カラム名
     * @return 結果の値
     */
    fun acquisitionValue(sql: String, parameters: List<Any>, label: String): Any? {
        return acquisitionValues(sql, parameters, mutableListOf(label)).getValue(label)
    }

    /**
     * 複数の行を取得する
     * @param sql 実行するSQL文
     * @param parameters パラメータリスト
     * @param mapper 結果セットの行をオブジェクトにマッピングする関数
     * @return 結果リスト
     */
    fun acquisitionValues(
        sql: String,
        parameters: List<Any>,
        keys: List<String>
    ): Map<String, Any?> {
        try {
            val values = mutableMapOf<String, Any?>()
            connection.use { conn ->
                conn.prepareStatement(sql).use { preparedStatement ->
                    parameters.bindParameters(preparedStatement)
                    preparedStatement.executeQuery().use { resultSet ->
                        if (resultSet.next()) {
                            for (key in keys) {
                                values[key] = try {
                                    resultSet.getString(key)
                                } catch (e: SQLException) {
                                    null
                                }
                            }
                        }
                    }
                }
            }
            return values
        } catch (e: SQLException) {
            println("SQL Error: ${e.message}")
            throw e
            return mapOf()
        }
    }

    fun acquisitionScheduleValue(sql: String): MutableList<ScheduleData> {
        val scheduleDataList = mutableListOf<ScheduleData>()

        try {
            connection.use { conn ->
                conn.prepareStatement(sql).use { preparedStatement ->

                    preparedStatement?.executeQuery()?.use { resultSet ->
                        while (resultSet.next()) {
                            val id = resultSet.getInt(Data.ID_KEY)
                            val scenarioName = resultSet.getString(Data.SCENARIO_NAME_KEY)
                            val datetime = resultSet.getString(Data.DATE_KEY)
                            val channelId = resultSet.getString(Data.CHANNEL_ID_KEY)
                            val status = resultSet.getInt(Data.STATUS_KEY)

                            val scheduleData = ScheduleData(id, scenarioName, datetime, channelId, status)
                            scheduleDataList.add(scheduleData)
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            // エラーハンドリング
            println("SQL Error: ${e.message}")
        }

        return scheduleDataList
    }

    // SQLiteコネクションの取得
    private val connection: Connection
        get() = DriverManager.getConnection("jdbc:sqlite:${Data.dbFilePath}")

    // パラメータをPreparedStatementにバインドする拡張関数
    private fun List<Any>.bindParameters(preparedStatement: PreparedStatement) {
        this.forEachIndexed { index, param ->
            preparedStatement.setObject(index + 1, param)
        }
    }
}