package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.util.*

suspend fun <T> operationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend (logger: IOperationLogger) -> T
): T {
    val logger = LoggerFactory.getLogger(operationName)
    val setting = OperationLoggerSetting(logger = logger).apply { configure() }
    val context = OperationLoggerContext(
        operation = Operation(
            name = operationName,
            id = operationId
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationLogger: IOperationLogger = OperationLogger(setting = setting, context = context)
    return runOperation(logger = operationLogger, operation = operation)
}

suspend fun <T> IOperationLogger.operationLog(
    operationName: String,
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend (logger: IOperationLogger) -> T
): T {
    val parentOperationLogger = this
    val subOperationName = "${parentOperationLogger.context.operation.name}.${operationName}"
    val logger = LoggerFactory.getLogger(subOperationName)
    val setting = parentOperationLogger.setting.copy(
        logger = logger,
        initialContextData = parentOperationLogger.context.data
    ).apply { configure() }
    val context = OperationLoggerContext(
        operation = Operation(
            name = subOperationName,
            id = parentOperationLogger.context.operation.id
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val operationLogger: IOperationLogger = OperationLogger(setting = setting, context = context)
    return runOperation(logger = operationLogger, operation = operation)
}

private fun generateOperationId(): String {
    return UUID.randomUUID().toString()
}

private suspend fun <T> runOperation(
    logger: IOperationLogger,
    operation: suspend (logger: IOperationLogger) -> T
): T {
    val result: T
    logger.context.operation.start()
    logger.log()
    try {
        logger.context.operation.execute()
        result = operation(logger)
        logger.context.operation.end()
        logger.log()
    } catch (e: Throwable) {
        logger.context.operation.exception(e)
        logger.log()
        throw e
    }
    return result
}
