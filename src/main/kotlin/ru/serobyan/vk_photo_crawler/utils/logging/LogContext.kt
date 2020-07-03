package ru.serobyan.vk_photo_crawler.utils.logging

import java.time.Instant

data class LogContext(
    val operation_name: String,
    val operation_id: String,
    var state: LogInformationState = LogInformationState.START,
    val data: MutableMap<String, Any?> = mutableMapOf(),
    var exception: Throwable? = null,
    val start_time: Long = Instant.now().epochSecond,
    var end_time: Long? = null,
    var execution_time: Long? = null,
    var time: Long = Instant.now().epochSecond
) {
    fun executionIsOver() {
        end_time = Instant.now().epochSecond
        execution_time = end_time!! - start_time
    }
}