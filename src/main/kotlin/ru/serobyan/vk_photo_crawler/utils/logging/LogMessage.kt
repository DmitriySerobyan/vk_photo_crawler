package ru.serobyan.vk_photo_crawler.utils.logging

import ru.serobyan.vk_photo_crawler.utils.json.toJSON

data class LogMessage(
    private val message: String? = null,
    private val logContext: LogContext
) {
    override fun toString(): String {
        return if (message != null) "$message ${logContext.toJSON()}" else logContext.toJSON()
    }
}