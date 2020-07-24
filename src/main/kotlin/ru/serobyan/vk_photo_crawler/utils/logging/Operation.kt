package ru.serobyan.vk_photo_crawler.utils.logging

import java.time.Instant

data class Operation(
    val name: String? = null,
    val id: String? = null,
    var state: OperationState? = null,
    val start_time: Long? = null,
    var end_time: Long? = null,
    var execution_time: Long? = null,
    var exception: Throwable? = null
) {
    fun executionIsOver() {
        end_time = Instant.now().epochSecond
        if (end_time != null && start_time != null) {
            execution_time = end_time!! - start_time
        }
    }
}