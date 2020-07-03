package ru.serobyan.vk_photo_crawler.service

import org.openqa.selenium.Cookie
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.utils.json.Json
import java.io.File

class CookieStorage(
    private val pathToCookieStorageFile: String = Config.pathToCookieStorage
) {
    fun save(cookies: Set<Cookie>) {
        val file = File(pathToCookieStorageFile)
        val content = cookies.joinToString("\n") { Json.toJson(it) }
        file.writeText(content)
    }

    fun read(): Set<Cookie> {
        val file = File(pathToCookieStorageFile)
        if(!file.exists()) file.createNewFile()
        return file
            .readLines()
            .map { cookie -> Json.fromJson<Cookie>(cookie) }
            .toSet()
    }
}