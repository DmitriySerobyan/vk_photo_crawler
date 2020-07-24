package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.event.Level

interface IOperationLogger {
    val setting: OperationLoggerSetting
    val context: OperationLoggerContext
    fun log(message: String? = null)
    fun log(level: Level, message: String? = null)
    fun put(key: String, value: Any?)
    fun inc(key: String, value: Long = 1)
}