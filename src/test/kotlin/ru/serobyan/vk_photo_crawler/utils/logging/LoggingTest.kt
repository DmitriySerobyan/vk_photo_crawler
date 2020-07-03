package ru.serobyan.vk_photo_crawler.utils.logging

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.event.Level

class LoggingTest: StringSpec({

    "!:manual 1" {
        log("find", { setAllLogLevel(Level.INFO) }) {
            log()
            delay(1000L)
            loggingData("user", 222)
            log()
        }
    }

    "!:manual 2" {
        val logger = Logger(setting = LogSetting().apply { setAllLogLevel(Level.INFO) })
        logger.loggingData("user", 222)
        logger.log()
    }

})