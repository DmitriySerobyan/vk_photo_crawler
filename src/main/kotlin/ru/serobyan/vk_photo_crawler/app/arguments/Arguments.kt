package ru.serobyan.vk_photo_crawler.app.arguments

import ru.serobyan.vk_photo_crawler.app.arguments.AppCommand.CRAWL_PHOTO_URLS
import ru.serobyan.vk_photo_crawler.app.arguments.AppCommand.DOWNLOAD_PHOTOS

data class Arguments(
    val commands: Set<AppCommand> = setOf(CRAWL_PHOTO_URLS, DOWNLOAD_PHOTOS),
    val photoLimit: Int = Int.MAX_VALUE,
    val groupUrl: String = "",
    val login: String = "",
    val password: String = ""
)