package ru.serobyan.vk_photo_crawler.utils.logging

import ru.serobyan.vk_photo_crawler.utils.json.toJSON

data class OperationLoggerMessage(
    private val message: String? = null,
    private val context: OperationLoggerContext
) {
    override fun toString(): String {
        return if (message != null) "$message ${context.toJSON()}" else context.toJSON()
    }
}