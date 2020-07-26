package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.util.*

suspend fun <T> IOperationLogger?.subOrRootOperationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend (logger: IOperationLogger) -> T
): T {
    return if(this != null) {
        this.subOperationLog(
            operationName = operationName,
            operationId = operationId,
            configure = configure,
            operation = operation
        )
    } else {
        operationLog(
            operationName = operationName,
            operationId = operationId,
            configure = configure,
            operation = operation
        )
    }
}

suspend fun <T> operationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend (logger: IOperationLogger) -> T
): T {
    val setting = OperationLoggerSetting(
        logger = LoggerFactory.getLogger(operationName)
    ).apply { configure() }
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

suspend fun <T> IOperationLogger.subOperationLog(
    operationName: String,
    operationId: String = generateOperationId(),
    configure: suspend OperationLoggerSetting.() -> Unit = {},
    operation: suspend (logger: IOperationLogger) -> T
): T {
    val parentLogger = this
    val subOperationName = "${parentLogger.context.operation.name}.${operationName}"
    val parentOperationIds = if (parentLogger.context.operation.parent_ids != null) {
        listOf(parentLogger.context.operation.id) + parentLogger.context.operation.parent_ids!!
    } else {
        listOf(parentLogger.context.operation.id)
    }
    val setting = parentLogger.setting.copy(
        logger = LoggerFactory.getLogger(subOperationName),
        initialContextData = parentLogger.context.data
    ).apply { configure() }
    val context = OperationLoggerContext(
        operation = Operation(
            name = subOperationName,
            id = operationId,
            parent_ids = parentOperationIds
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
