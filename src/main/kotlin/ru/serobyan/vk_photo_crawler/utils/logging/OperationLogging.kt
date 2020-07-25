package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

suspend fun <T> operationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend IOperationLogger.() -> T
): T {
    val logger = LoggerFactory.getLogger(operationName)
    val setting = OperationLoggerSetting(logger = logger).apply { configure() }
    val context = OperationLoggerContext(
        operation = Operation(
            name = operationName,
            id = operationId,
            state = OperationState.START
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationLogger: IOperationLogger = OperationLogger(setting = setting, context = context)
    return operationLogger.runOperation(operation)
}

suspend fun <T> IOperationLogger.subOperationLog(
    operationName: String,
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend IOperationLogger.() -> T
): T {
    val parentOperationLogger = this
    val subOperationName = "${parentOperationLogger.context.operation.name}/${operationName}"
    val logger = LoggerFactory.getLogger(subOperationName)
    val setting = parentOperationLogger.setting.copy(
        logger = logger,
        initialContextData = parentOperationLogger.context.data
    ).apply { configure() }
    val context = OperationLoggerContext(
        operation = Operation(
            name = subOperationName,
            id = parentOperationLogger.context.operation.id,
            state = OperationState.START
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationLogger: IOperationLogger = OperationLogger(setting = setting, context = context)
    return operationLogger.runOperation(operation)
}

private fun generateOperationId(): String {
    return UUID.randomUUID().toString()
}

private suspend fun <T> IOperationLogger.runOperation(operation: suspend IOperationLogger.() -> T): T {
    val result: T
    context.operation.start()
    log()
    try {
        context.operation.execute()
        result = operation()
        context.operation.end()
        log()
    } catch (e: Throwable) {
        context.operation.exception(e)
        log()
        throw e
    }
    return result
}
