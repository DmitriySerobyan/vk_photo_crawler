package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import com.google.gson.JsonElement
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import net.lightbody.bmp.BrowserMobProxy
import org.openqa.selenium.TimeoutException
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.selenium.alert
import ru.serobyan.vk_photo_crawler.selenium.proxy.getHarEntryByUrl
import ru.serobyan.vk_photo_crawler.selenium.scrollBy
import ru.serobyan.vk_photo_crawler.selenium.waitUntil
import ru.serobyan.vk_photo_crawler.utils.json.fromJSON
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog

class VkGroupPhotoIdsGetter(
    private val driver: WebDriver,
    private val proxy: BrowserMobProxy
) {
    suspend fun getPhotoIds(context: VkGroupPhotoIdsGetterContext): Flow<String> {
        return context.logger.subOrRootOperationLog("get_vk_group_photo_ids", configure = {
            put("group_url", context.groupUrl)
        }) { logger ->
            flow {
                driver.get(context.groupUrl)
                val initialPostIds = VkPhotoIdsParser.parseGroupMainPage(html = driver.pageSource)
                logger.put("initial_post_ids", initialPostIds)
                initialPostIds.forEach { emit(it) }
                logger.log()
                try {
                    while (true) emitAll(getMorePhotoIds(logger = logger))
                } catch (e: TimeoutException) {
                    logger.put("no_more_posts", true)
                    logger.log("Can't get more posts")
                    driver.alert("Can't get more posts")
                    delay(5_000L)
                }
            }
        }
    }

    private suspend fun getMorePhotoIds(logger: IOperationLogger): Flow<String> {
        return logger.subOperationLog("get_more_photo_ids") { subLogger ->
            flow {
                proxy.newHar()
                scroll(logger = subLogger)
                driver.waitUntil { proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl).isNotEmpty() }
                val harEntries = proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl)
                harEntries.forEach { harEntry ->
                    val response: String? = harEntry.response.content.text
                    if (response != null) {
                        val html = getHtmlResultFromMorePostResponse(logger = subLogger, response = response)
                        val photoIds =
                            VkPhotoIdsParser.parseMorePostResponse(
                                html = html
                            )
                        subLogger.put("photo_ids", photoIds)
                        photoIds.forEach { emit(it) }
                    }
                }
                proxy.endHar()
            }
        }
    }

    private suspend fun scroll(logger: IOperationLogger) {
        logger.subOperationLog("scroll") {
            repeat(10) {
                driver.scrollBy(y = 10_000)
                delay(100L)
            }
        }
    }

    private suspend fun getHtmlResultFromMorePostResponse(logger: IOperationLogger, response: String): String {
        return logger.subOperationLog("get_html_result_from_more_post_response") { subLogger ->
            val jsonElement = fromJSON<JsonElement>(response)
            val html = jsonElement.asJsonObject["payload"].asJsonArray[1].asJsonArray[0].asString
            subLogger.put("html", html)
            html
        }
    }
}