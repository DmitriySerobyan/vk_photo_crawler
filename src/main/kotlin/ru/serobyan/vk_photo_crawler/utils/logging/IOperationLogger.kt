package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level

interface IOperationLogger {
    val logSetting: LogSetting
    val logContext: LogContext
    fun log(message: String? = null)
    fun log(level: Level, message: String? = null)
    fun loggingData(key: String, value: Any?)
    fun incrementCounter(key: String, value: Long = 1)
}