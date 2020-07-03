package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.utils.json.Json
import java.time.Instant

interface IOperationLogDslContext {
    fun log(message: String? = null)
    fun log(level: Level, message: String? = null)
    fun logData(key: String, value: Any?)
}

class OperationLogDslContext(
    private val setting: LogSetting,
    private val context: LogContext
) : IOperationLogDslContext {
    override fun log(message: String?) {
        log(level = setting.executeLogLevel, message = message)
    }

    override fun log(level: Level, message: String?) {
        context.time = Instant.now().epochSecond
        val logText = if (message != null) "$message ${Json.toJson(context)}" else Json.toJson(context)
        with(setting.logger) {
            when (level) {
                Level.TRACE -> trace(logText)
                Level.DEBUG -> debug(logText)
                Level.INFO -> info(logText)
                Level.WARN -> warn(logText)
                Level.ERROR -> error(logText)
            }
        }
    }

    override fun logData(key: String, value: Any?) {
        context.data[key] = value
    }
}