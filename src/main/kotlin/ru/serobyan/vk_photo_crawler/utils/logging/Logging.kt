package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

suspend fun <T> IOperationLogger.subOperationLog(
    operationName: String,
    configure: suspend LogSetting.() -> Unit = {},
    operation: suspend IOperationLogger.() -> T
): T {
    val parentOperationLogger = this
    val subOperationName = "${parentOperationLogger.logContext.operation.name}/${operationName}"
    val logger = LoggerFactory.getLogger(subOperationName)
    val setting = parentOperationLogger.logSetting.copy(
        logger = logger,
        initialContextData = parentOperationLogger.logContext.data
    ).apply { configure() }
    val context = LogContext(
        operation = Operation(
            name = subOperationName,
            id = parentOperationLogger.logContext.operation.id,
            state = OperationState.START,
            start_time = Instant.now().epochSecond
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationOperationLogger: IOperationLogger = OperationLogger(logSetting = setting, logContext = context)
    return operationOperationLogger.runOperation(operation)
}

suspend fun <T> operationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend LogSetting.() -> Unit = {},
    operation: suspend IOperationLogger.() -> T
): T {
    val logger = LoggerFactory.getLogger(operationName)
    val setting = LogSetting(logger = logger).apply { configure() }
    val context = LogContext(
        operation = Operation(
            name = operationName,
            id = operationId,
            state = OperationState.START,
            start_time = Instant.now().epochSecond
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationOperationLogger: IOperationLogger = OperationLogger(logSetting = setting, logContext = context)
    return operationOperationLogger.runOperation(operation)
}

private fun generateOperationId(): String {
    return UUID.randomUUID().toString()
}

private suspend fun <T> IOperationLogger.runOperation(operation: suspend IOperationLogger.() -> T): T {
    val result: T
    log(level = logSetting.startLogLevel)
    try {
        logContext.operation.state = OperationState.EXECUTE
        result = operation()
        logContext.operation.executionIsOver()
        logContext.operation.state = OperationState.END
        log(level = logSetting.endLogLevel)
    } catch (e: Throwable) {
        logContext.operation.executionIsOver()
        logContext.operation.exception = e
        logContext.operation.state = OperationState.EXCEPTION
        log(level = logSetting.exceptionLogLevel)
        throw e
    }
    return result
}
