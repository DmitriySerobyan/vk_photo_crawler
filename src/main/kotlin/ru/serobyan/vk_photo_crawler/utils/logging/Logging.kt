package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

suspend fun <T> log(
    operationName: String,
    operation: suspend ILogger.() -> T
): T {
    return log(
        operationName = operationName,
        operationId = generateOperationId(),
        configureLog = {},
        operation = operation
    )
}

suspend fun <T> log(
    operationName: String,
    configureLog: suspend LogSetting.() -> Unit,
    operation: suspend ILogger.() -> T
): T {
    return log(
        operationName = operationName,
        operationId = generateOperationId(),
        configureLog = configureLog,
        operation = operation
    )
}

suspend fun <T> log(
    operationName: String,
    operationId: String,
    operation: suspend ILogger.() -> T
): T {
    return log(
        operationName = operationName,
        operationId = operationId,
        configureLog = {},
        operation = operation
    )
}

suspend fun <T> log(
    operationName: String,
    operationId: String = generateOperationId(),
    configureLog: suspend LogSetting.() -> Unit,
    operation: suspend ILogger.() -> T
): T {
    var operationResult: T? = null
    val logger = LoggerFactory.getLogger(operationName)
    val setting = LogSetting(logger = logger).apply {
        configureLog()
    }
    val context = LogContext(
        operation = Operation(
            name = operationName,
            id = operationId,
            state = OperationState.START,
            start_time = Instant.now().epochSecond
        ),
        data = setting.initialContextData.toMutableMap()
    )
    val dsl: ILogger = Logger(setting = setting, context = context)
    with(dsl) {
        log(level = setting.startLogLevel)
        try {
            context.operation.state = OperationState.EXECUTE
            operationResult = operation()
            context.operation.executionIsOver()
            context.operation.state = OperationState.END
            log(level = setting.endLogLevel)
        } catch (e: Throwable) {
            context.operation.executionIsOver()
            context.operation.exception = e
            context.operation.state = OperationState.EXCEPTION
            log(level = setting.exceptionLogLevel)
            throw e
        }
    }
    return operationResult!!
}

internal fun generateOperationId(): String {
    return UUID.randomUUID().toString()
}
