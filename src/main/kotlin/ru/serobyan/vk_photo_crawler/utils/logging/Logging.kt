package ru.serobyan.vk_photo_crawler.utils.logging

import org.slf4j.LoggerFactory
import java.util.*

suspend fun <T> log(
    operationName: String,
    operation: suspend IOperationLogDslContext.() -> T
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
    operation: suspend IOperationLogDslContext.() -> T
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
    operation: suspend IOperationLogDslContext.() -> T
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
    operation: suspend IOperationLogDslContext.() -> T
): T {
    var operationResult: T? = null
    val setting = LogSetting().apply {
        configureLog()
        if (!isLoggerSet) logger = LoggerFactory.getLogger(operationName)
    }
    val context = LogContext(
        operation_name = operationName,
        operation_id = operationId,
        data = setting.initialContextData.toMutableMap()
    )
    val dsl: IOperationLogDslContext = OperationLogDslContext(setting = setting, context = context)
    with(dsl) {
        log(level = setting.startLogLevel)
        try {
            context.state = LogInformationState.EXECUTE
            operationResult = operation()
            context.executionIsOver()
            context.state = LogInformationState.END
            log(level = setting.endLogLevel)
        } catch (e: Throwable) {
            context.executionIsOver()
            context.exception = e
            context.state = LogInformationState.EXCEPTION
            log(level = setting.exceptionLogLevel)
            throw e
        }
    }
    return operationResult!!
}

internal fun generateOperationId(): String {
    return UUID.randomUUID().toString()
}
