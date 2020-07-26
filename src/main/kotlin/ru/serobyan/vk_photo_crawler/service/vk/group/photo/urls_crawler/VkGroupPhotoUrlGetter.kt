package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntilVisibility
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog
import kotlin.random.Random

class VkGroupPhotoUrlGetter(
    private val driver: WebDriver
) {
    suspend fun getPhotoUrl(context: VkGroupPhotoUrlGetterContext): String {
        return context.logger.subOrRootOperationLog("get_vk_group_photo_url", configure = {
            put("group_url", context.groupUrl)
            put("photo_id", context.photoId)
        }) { logger ->
            val postUrl = getPostUrlWithPhotoUrl(groupUrl = context.groupUrl, photoId = context.photoId)
            logger.put("post_url", postUrl)
            driver.get(postUrl)
            val image = driver.getElement(imageBy)
            val photoUrl = image.getAttribute("src")
            logger.put("photo_url", photoUrl)
            delay()
            photoUrl
        }
    }

    private suspend fun getPhotoUrlFromOpenOriginalButton(logger: IOperationLogger, groupUrl: String, photoId: String): String {
        return logger.subOperationLog("get_photo_url_from_open_original_button") { subLogger ->
            val postUrl = getPostUrlWithPhotoUrl(groupUrl = groupUrl, photoId = photoId)
            driver.get(postUrl)
            subLogger.put("post_url", postUrl)
            val buttonMore = driver.getElement(buttonMoreBy)
            val actionsBuilder = Actions(driver)
            actionsBuilder.moveToElement(buttonMore).build().perform()
            driver.waitUntilVisibility(selector = linkOpenOriginalBy)
            val linkOpenOriginal = driver.getElement(linkOpenOriginalBy)
            val photoUrl = linkOpenOriginal.getAttribute("href")
            subLogger.put("photo_url", photoUrl)
            delay()
            photoUrl
        }
    }

    private fun getPostUrlWithPhotoUrl(groupUrl: String, photoId: String): String {
        return "${groupUrl}?z=photo${photoId}"
    }

    private suspend fun delay() {
        delay(
            Random.nextLong(
                from = Config.minDelayBetweenOpenPhotoPost,
                until = Config.maxDelayBetweenOpenPhotoPost
            )
        )
    }

    companion object {
        val imageBy: By = By.cssSelector("#pv_photo img")
        val buttonMoreBy: By = By.cssSelector(".pv_actions_more")
        val linkOpenOriginalBy: By = By.id("pv_more_act_download")
    }
}