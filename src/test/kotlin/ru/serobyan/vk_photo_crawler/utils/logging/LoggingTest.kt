package ru.serobyan.vk_photo_crawler.utils.logging

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.event.Level

class LoggingTest: StringSpec({

    "!:manual 1" {
        log("find", configureLog = { setAllLogLevel(Level.INFO) }) {
            log()
            delay(1000L)
            loggingData("user", 222)
            log()
        }
    }

})