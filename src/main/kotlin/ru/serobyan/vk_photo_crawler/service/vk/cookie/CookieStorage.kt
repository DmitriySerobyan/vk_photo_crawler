package ru.serobyan.vk_photo_crawler.service.vk.cookie

import org.openqa.selenium.Cookie
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.utils.json.*
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog
import java.io.File

class CookieStorage(
    private val pathToCookieStorageFile: String = Config.pathToCookieStorage
) {
    suspend fun save(cookies: Set<Cookie>) {
        operationLog("cookie_storage_save", configure = {
            loggingData("path_to_cookie_storage_file", pathToCookieStorageFile)
            loggingData("cookies", cookies)
        }) {
            val file = File(pathToCookieStorageFile)
            val content = cookies.joinToString("\n") { it.toJSON() }
            file.writeText(content)
        }
    }

    suspend fun read(): Set<Cookie> {
        return operationLog("cookie_storage_read", configure = {
            loggingData("path_to_cookie_storage_file", pathToCookieStorageFile)
        }) {
            val file = File(pathToCookieStorageFile)
            if (!file.exists()) file.createNewFile()
            val cookies = file
                .readLines()
                .map { cookie -> fromJSON<Cookie>(cookie) }
                .toSet()
            put("cookies", cookies)
            cookies
        }
    }
}