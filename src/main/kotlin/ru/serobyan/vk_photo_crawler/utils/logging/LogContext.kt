package ru.serobyan.vk_photo_crawler.utils.logging

import java.time.Instant

data class LogContext(
    val operation: Operation = Operation(),
    val data: MutableMap<String, Any?> = mutableMapOf(),
    var time: Long = Instant.now().epochSecond
)