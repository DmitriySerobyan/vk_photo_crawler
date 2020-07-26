package ru.serobyan.vk_photo_crawler.utils.logging

import java.time.Instant

data class Operation(
    val name: String,
    val id: String,
    val parent_ids: List<String>? = null,
    var state: OperationState? = null,
    var start_time: Long? = null,
    var end_time: Long? = null,
    var execution_time: Long? = null,
    var error_description: String? = null
) {
    @Transient
    var exception: Throwable? = null

    fun start() {
        start_time = Instant.now().epochSecond
        state = OperationState.START
    }

    fun execute() {
        state = OperationState.EXECUTE
    }

    fun end() {
        state = OperationState.END
        operationIsOver()
    }

    fun exception(e: Throwable) {
        exception = e
        error_description = e.toString()
        state = OperationState.EXCEPTION
        operationIsOver()
    }

    private fun operationIsOver() {
        end_time = Instant.now().epochSecond
        execution_time = end_time!! - start_time!!
    }
}