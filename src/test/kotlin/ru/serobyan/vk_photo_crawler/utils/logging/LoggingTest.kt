package ru.serobyan.vk_photo_crawler.utils.logging

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.event.Level

class LoggingTest: StringSpec({

    "!:manual" {
        log("mySave", { setAllLogLevel(Level.WARN) }) {
            log()
            delay(1000L)
            logData("myLogData", 222)
            log()
        }
    }

})