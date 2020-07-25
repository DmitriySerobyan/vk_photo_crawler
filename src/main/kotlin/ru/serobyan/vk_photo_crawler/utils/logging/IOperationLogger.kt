package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level

interface IOperationLogger {
    val setting: OperationLoggerSetting
    val context: OperationLoggerContext

    fun log() {
        log(level = null, message = null, throwable = null)
    }

    fun log(message: String? = null) {
        log(level = null, message = message, throwable = null)
    }

    fun log(throwable: Throwable? = null) {
        log(level = null, message = null, throwable = throwable)
    }

    fun log(message: String? = null, throwable: Throwable? = null) {
        log(level = null, message = message, throwable = throwable)
    }

    fun log(level: Level? = null) {
        log(level = level, message = null, throwable = null)
    }

    fun log(level: Level? = null, message: String? = null) {
        log(level = level, message = message, throwable = null)
    }

    fun log(level: Level? = null, throwable: Throwable? = null) {
        log(level = level, message = null, throwable = throwable)
    }

    fun log(level: Level? = null, message: String? = null, throwable: Throwable? = null)

    fun put(loggingData: String, value: Any? = null)
    fun inc(counter: String, value: Long = 1)
}