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
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog

class VkGroupPhotoIdsGetter(
    private val driver: WebDriver,
    private val proxy: BrowserMobProxy
) {
    suspend fun getPhotoIds(context: VkGroupPhotoIdsGetterContext): Flow<String> {
        return context.operationLogger.subOperationLog("get_vk_group_photo_ids", configure = {
            put("group_url", context.groupUrl)
        }) {
            flow {
                driver.get(context.groupUrl)
                val initialPostIds = VkPhotoIdsParser.parseGroupMainPage(html = driver.pageSource)
                put("initial_post_ids", initialPostIds)
                initialPostIds.forEach { emit(it) }
                log()
                try {
                    while (true) emitAll(context.getMorePhotoIds())
                } catch (e: TimeoutException) {
                    put("no_more_posts", true)
                    log("Can't get more posts")
                    driver.alert("Can't get more posts")
                    delay(5_000L)
                }
            }
        }
    }

    private suspend fun VkGroupPhotoIdsGetterContext.getMorePhotoIds(): Flow<String> {
        return operationLogger.subOperationLog("get_more_photo_ids") {
            flow {
                proxy.newHar()
                scroll()
                driver.waitUntil { proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl).isNotEmpty() }
                val harEntries = proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl)
                harEntries.forEach { harEntry ->
                    val response: String? = harEntry.response.content.text
                    if (response != null) {
                        val html = getHtmlResultFromMorePostResponse(response = response)
                        val photoIds =
                            VkPhotoIdsParser.parseMorePostResponse(
                                html = html
                            )
                        put("photo_ids", photoIds)
                        photoIds.forEach { emit(it) }
                    }
                }
                proxy.endHar()
            }
        }
    }

    private suspend fun VkGroupPhotoIdsGetterContext.scroll() {
        operationLogger.subOperationLog("scroll") {
            repeat(10) {
                driver.scrollBy(y = 10_000)
                delay(100L)
            }
        }
    }

    private suspend fun VkGroupPhotoIdsGetterContext.getHtmlResultFromMorePostResponse(response: String): String {
        return operationLogger.subOperationLog("get_html_result_from_more_post_response") {
            val jsonElement = fromJSON<JsonElement>(response)
            val html = jsonElement.asJsonObject["payload"].asJsonArray[1].asJsonArray[0].asString
            put("html", html)
            html
        }
    }
}