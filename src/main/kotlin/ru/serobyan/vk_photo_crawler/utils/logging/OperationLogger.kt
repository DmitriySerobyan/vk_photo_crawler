package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.utils.json.Json
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class OperationLogger(
    override val logSetting: LogSetting = LogSetting(),
    override val logContext: LogContext = LogContext()
) : IOperationLogger {
    override fun log(message: String?) {
        log(level = logSetting.executeLogLevel, message = message)
    }

    override fun log(level: Level, message: String?) {
        logContext.time = Instant.now().epochSecond
        val logText = if (message != null) "$message ${Json.toJson(logContext)}" else Json.toJson(logContext)
        with(logSetting.logger) {
            when (level) {
                Level.TRACE -> trace(logText)
                Level.DEBUG -> debug(logText)
                Level.INFO -> info(logText)
                Level.WARN -> warn(logText)
                Level.ERROR -> error(logText)
            }
        }
    }

    override fun loggingData(key: String, value: Any?) {
        logContext.data[key] = value
    }

    override fun incrementCounter(key: String, value: Long) {
        val atomic = logContext.counters[key]
        if (atomic != null) {
            atomic.addAndGet(value)
        } else {
            logContext.counters.putIfAbsent(key, AtomicLong(0L))
            logContext.counters[key]!!.addAndGet(value)
        }
    }
}