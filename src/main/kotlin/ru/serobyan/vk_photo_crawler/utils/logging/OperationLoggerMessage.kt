package ru.serobyan.vk_photo_crawler.utils.logging

import ru.serobyan.vk_photo_crawler.utils.json.toJSON

data class OperationLoggerMessage(
    private val message: String? = null,
    private val operationLoggerContext: OperationLoggerContext
) {
    override fun toString(): String {
        return if (message != null) "$message ${operationLoggerContext.toJSON()}" else operationLoggerContext.toJSON()
    }
}