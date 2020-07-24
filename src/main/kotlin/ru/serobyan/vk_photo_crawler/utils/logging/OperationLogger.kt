package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class OperationLogger(
    override val setting: OperationLoggerSetting = OperationLoggerSetting(),
    override val context: OperationLoggerContext = OperationLoggerContext()
) : IOperationLogger {
    override fun log(message: String?) {
        log(level = setting.executeLogLevel, message = message)
    }

    override fun log(level: Level, message: String?) {
        context.time = Instant.now().epochSecond
        val logMessage = OperationLoggerMessage(
            message = message,
            operationLoggerContext = context
        )
        with(setting.logger) {
            when (level) {
                Level.TRACE -> trace("{}", logMessage)
                Level.DEBUG -> debug("{}", logMessage)
                Level.INFO -> info("{}", logMessage)
                Level.WARN -> warn("{}", logMessage)
                Level.ERROR -> error("{}", logMessage)
            }
        }
    }

    override fun put(key: String, value: Any?) {
        context.data[key] = value
    }

    override fun inc(key: String, value: Long) {
        val atomic = context.counters[key]
        if (atomic != null) {
            atomic.addAndGet(value)
        } else {
            context.counters.putIfAbsent(key, AtomicLong(0L))
            context.counters[key]!!.addAndGet(value)
        }
    }
}