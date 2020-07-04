package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntilVisibility
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import kotlin.random.Random

class VkGroupPhotoUrlGetter(
    private val driver: WebDriver
) {
    suspend fun getPhotoUrl(context: VkGroupPhotoUrlGetterContext): String {
        return context.operationLogger.subOperationLog("get_vk_group_photo_url", configure = {
            loggingData("group_url", context.groupUrl)
            loggingData("photo_id", context.photoId)
        }) {
            val postUrl = context.getPostUrlWithPhotoUrl()
            loggingData("post_url", postUrl)
            driver.get(postUrl)
            val image = driver.getElement(imageBy)
            val photoUrl = image.getAttribute("src")
            loggingData("photo_url", photoUrl)
            delay()
            photoUrl
        }
    }

    private suspend fun VkGroupPhotoUrlGetterContext.getPhotoUrlFromOpenOriginalButton(): String {
        return operationLogger.subOperationLog("get_photo_url_from_open_original_button", configure = {
            loggingData("group_url", groupUrl)
            loggingData("photo_id", photoId)
        }) {
            val postUrl = getPostUrlWithPhotoUrl()
            driver.get(postUrl)
            loggingData("post_url", postUrl)
            val buttonMore = driver.getElement(buttonMoreBy)
            val actionsBuilder = Actions(driver)
            actionsBuilder.moveToElement(buttonMore).build().perform()
            driver.waitUntilVisibility(selector = linkOpenOriginalBy)
            val linkOpenOriginal = driver.getElement(linkOpenOriginalBy)
            val photoUrl = linkOpenOriginal.getAttribute("href")
            loggingData("photo_url", photoUrl)
            delay()
            photoUrl
        }
    }

    private fun VkGroupPhotoUrlGetterContext.getPostUrlWithPhotoUrl(): String {
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