package ru.serobyan.vk_photo_crawler.service.vk

import kotlinx.coroutines.delay
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.interactions.Actions
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntilVisibility
import kotlin.random.Random

class VkGroupPhotoUrlGetter(
    private val driver: WebDriver
) {
    suspend fun getPhotoUrl(groupUrl: String, photoId: String): String {
        val postUrl = getPostUrlWithPhotoUrl(groupUrl = groupUrl, photoId = photoId)
        driver.get(postUrl)
        val image = driver.getElement(imageBy)
        delay()
        return image.getAttribute("src")
    }

    private suspend fun getPhotoUrlFromOpenOriginal(groupUrl: String, photoId: String): String {
        val postUrl = getPostUrlWithPhotoUrl(groupUrl = groupUrl, photoId = photoId)
        driver.get(postUrl)
        val buttonMore = driver.getElement(buttonMoreBy)
        val actionsBuilder = Actions(driver)
        actionsBuilder.moveToElement(buttonMore).build().perform()
        driver.waitUntilVisibility(selector = linkOpenOriginalBy)
        val linkOpenOriginal = driver.getElement(linkOpenOriginalBy)
        delay()
        return linkOpenOriginal.getAttribute("href")
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