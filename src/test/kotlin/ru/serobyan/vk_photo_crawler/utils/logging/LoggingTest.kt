package ru.serobyan.vk_photo_crawler.utils.logging

import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.delay
import org.slf4j.event.Level

class LoggingTest: StringSpec({

    "!:manual 1" {
        operationLog("find", configure = { setAllLogLevel(Level.INFO) }) {
            log()
            delay(1000L)
            put("user", 222)
            subOperationLog("notify") {
                delay(1000L)
                put("alert", 555)
            }
            log()
        }
    }

})