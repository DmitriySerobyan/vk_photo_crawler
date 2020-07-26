package ru.serobyan.vk_photo_crawler.utils.logging

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.event.Level
import java.lang.IllegalStateException

class LoggingTest: StringSpec({

    "!:manual 1" {
        operationLog("find", configure = { setAllLogLevel(Level.INFO) }) { parentLogger ->
            parentLogger.log()
            delay(1000L)
            parentLogger.put("user", 222)
            parentLogger.operationLog("notify") {logger ->
                delay(1000L)
                logger.put("alert", 555)
                logger.log()
            }
            try {
                parentLogger.operationLog("error") {
                    throw IllegalStateException()
                }
            } catch (_: IllegalStateException) {}
            parentLogger.log()
        }
    }

})