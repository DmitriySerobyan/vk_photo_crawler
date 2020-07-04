package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

data class LogSetting(
    val logger: Logger = LoggerFactory.getLogger(LogSetting::class.java),
    var startLogLevel: Level = Level.DEBUG,
    var executeLogLevel: Level = Level.TRACE,
    var endLogLevel: Level = Level.DEBUG,
    var exceptionLogLevel: Level = Level.ERROR,
    val initialContextData: MutableMap<String, Any?> = mutableMapOf()
) {
    fun setAllLogLevel(level: Level) {
        startLogLevel = level
        executeLogLevel = level
        endLogLevel = level
    }

    fun loggingData(key: String, value: Any?) {
        initialContextData[key] = value
    }
}