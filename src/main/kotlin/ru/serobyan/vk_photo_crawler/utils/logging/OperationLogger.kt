package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level
import java.lang.IllegalStateException
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

class OperationLogger(
    override val setting: OperationLoggerSetting = OperationLoggerSetting(),
    override val context: OperationLoggerContext = OperationLoggerContext()
) : IOperationLogger {
    override fun log(level: Level?, message: String?, throwable: Throwable?) {
        context.time = Instant.now().epochSecond
        val currentLevel = level ?: getCurrentOperationStateLogLevel()
        val currentThrowable = throwable ?: context.operation.exception
        val currentMessage = OperationLoggerMessage(message = message, context = context)
        with(setting.logger) {
            if (currentThrowable != null) {
                when (currentLevel) {
                    Level.TRACE -> trace(currentMessage.toString(), currentThrowable)
                    Level.DEBUG -> debug(currentMessage.toString(), currentThrowable)
                    Level.INFO -> info(currentMessage.toString(), currentThrowable)
                    Level.WARN -> warn(currentMessage.toString(), currentThrowable)
                    Level.ERROR -> error(currentMessage.toString(), currentThrowable)
                }
            } else {
                when (currentLevel) {
                    Level.TRACE -> trace("{}", currentMessage)
                    Level.DEBUG -> debug("{}", currentMessage)
                    Level.INFO -> info("{}", currentMessage)
                    Level.WARN -> warn("{}", currentMessage)
                    Level.ERROR -> error("{}", currentMessage)
                }
            }
        }
    }

    private fun getCurrentOperationStateLogLevel(): Level {
        return when (context.operation.state) {
            OperationState.START -> setting.startLogLevel
            OperationState.EXECUTE -> setting.executeLogLevel
            OperationState.END -> setting.endLogLevel
            OperationState.EXCEPTION -> setting.exceptionLogLevel
            null -> throw IllegalStateException("Logging operation state is 'null'")
        }
    }

    override fun put(loggingData: String, value: Any?) {
        context.data[loggingData] = value
    }

    override fun inc(counter: String, value: Long) {
        val atomic = context.counters[counter]
        if (atomic != null) {
            atomic.addAndGet(value)
        } else {
            context.counters.putIfAbsent(counter, AtomicLong(0L))
            context.counters[counter]!!.addAndGet(value)
        }
    }
}