package ru.serobyan.vk_photo_crawler.app

import ru.serobyan.vk_photo_crawler.app.AppCommand.*

enum class AppCommand(val priority: Int) {
    CRAWL_PHOTO_IDS(priority = 1),
    CRAWL_PHOTO_URLS(priority = 2),
    DOWNLOAD_PHOTOS(priority = 3),
}

data class Arguments(
    val commands: Set<AppCommand> = setOf(CRAWL_PHOTO_URLS, DOWNLOAD_PHOTOS),
    val groupUrl: String = "",
    val login: String = "",
    val password: String = ""
)

object ArgumentsParser{
    fun parse(args: Array<String>): Arguments {
        return Arguments()
    }
}