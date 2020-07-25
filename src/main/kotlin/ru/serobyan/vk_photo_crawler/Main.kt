package ru.serobyan.vk_photo_crawler

import kotlinx.coroutines.runBlocking
import org.kodein.di.instance
import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.app.App
import ru.serobyan.vk_photo_crawler.app.arguments.ArgumentsParser
import ru.serobyan.vk_photo_crawler.di.di
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog

fun main(args: Array<String>)  {
    runBlocking {
        operationLog("main", configure = { setAllLogLevel(Level.INFO) }) {
            val arguments = ArgumentsParser.parse(args = args)
            val app by di.instance<App>()
            app.use { it.run(arguments) }
        }
    }
}