package ru.serobyan.vk_photo_crawler.service.vk

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
import ru.serobyan.vk_photo_crawler.utils.json.Json


class VkGroupPhotoIdsGetter(
    private val driver: WebDriver,
    private val proxy: BrowserMobProxy
) {
    suspend fun getPhotoIds(groupUrl: String): Flow<String> {
        return flow {
            driver.get(groupUrl)
            val initialPostUrls =
                VkPhotoIdsParser.parseGroupMainPage(html = driver.pageSource)
            initialPostUrls.forEach { emit(it) }
            try {
                while (true) emitAll(getMorePhotoIds())
            } catch (e: TimeoutException) {
                println(Json.toJson(e, pretty = true))
                driver.alert("Can't get more posts")
                delay(5000L)
            }
        }
    }

    private suspend fun getMorePhotoIds(): Flow<String> {
        return flow {
            proxy.newHar()
            scroll()
            driver.waitUntil { proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl).isNotEmpty() }
            val harEntries = proxy.getHarEntryByUrl(url = Config.vkMorePostRequestUrl)
            harEntries.forEach { harEntry ->
                val response: String? = harEntry.response.content.text
                if (response != null) {
                    val html = getHtmlResultFromMorePostResponse(response)
                    val photoIds =
                        VkPhotoIdsParser.parseMorePostResponse(html = html)
                    photoIds.forEach { emit(it) }
                }
            }
            proxy.endHar()
        }
    }

    private suspend fun scroll() {
        repeat(10) {
            driver.scrollBy(y = 10_000)
            delay(100L)
        }
    }

    private fun getHtmlResultFromMorePostResponse(response: String): String {
        val jsonElement = Json.fromJson<JsonElement>(response)
        return jsonElement.asJsonObject["payload"].asJsonArray[1].asJsonArray[0].asString
    }
}