package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.openqa.selenium.TimeoutException
import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.model.VkPhoto
import ru.serobyan.vk_photo_crawler.model.VkPhotoState
import ru.serobyan.vk_photo_crawler.model.VkPhotoTable
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginService
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginServiceContext
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog

class VkGroupPhotoUrlsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoUrlGetter: VkGroupPhotoUrlGetter
) {
    suspend fun crawlPhotoUrls(context: VkGroupPhotoUrlsCrawlerContext) {
        operationLog("crawl_vk_group_photo_urls", configure = {
            loggingData("login", context.login)
            loggingData("group_url", context.groupUrl)
        }) {
            context.operationLogger = this
            vkLoginService.login(
                VkLoginServiceContext(
                    operationLogger = context.operationLogger,
                    login = context.login,
                    password = context.password
                )
            )
            while (true) {
                val vkPhotos = context.getVkPhotos()
                if (vkPhotos.isEmpty()) break
                vkPhotos.forEach { vkPhoto ->
                    val photoUrl = context.getPhotoUrl(vkPhoto = vkPhoto)
                    if (photoUrl == null) {
                        context.setErrorState(vkPhoto = vkPhoto)
                    } else {
                        context.savePhotoUrl(vkPhoto = vkPhoto, photoUrl = photoUrl)
                    }
                }
            }
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.getVkPhotos(): List<VkPhoto> {
        return operationLogger.subOperationLog("get_vk_photos") {
            val vkPhotos = transaction {
                VkPhoto
                    .find { (VkPhotoTable.state eq VkPhotoState.PHOTO_ID_SAVED) and (VkPhotoTable.groupUrl eq groupUrl) }
                    .limit(100)
                    .toList()
            }
            loggingData("vk_photos", vkPhotos)
            vkPhotos
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.getPhotoUrl(vkPhoto: VkPhoto): String? {
        return operationLogger.subOperationLog("get_photo_url", configure = {
            loggingData("vk_photo", vkPhoto)
        }) {
            try {
                val photoUrl = vkGroupPhotoUrlGetter.getPhotoUrl(
                    VkGroupPhotoUrlGetterContext(
                        operationLogger = this,
                        groupUrl = vkPhoto.groupUrl,
                        photoId = vkPhoto.photoId
                    )
                )
                loggingData("photo_url", photoUrl)
                photoUrl
            } catch (_: TimeoutException) {
                loggingData("photo_url_received", true)
                log(Level.WARN, "failed to get photo_url")
                null
            }
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.setErrorState(vkPhoto: VkPhoto) {
        operationLogger.subOperationLog("set_error_state", configure = {
            loggingData("vk_photo", vkPhoto)
        }) {
            transaction {
                vkPhoto.state = VkPhotoState.PHOTO_URL_ERROR
            }
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.savePhotoUrl(vkPhoto: VkPhoto, photoUrl: String) {
        operationLogger.subOperationLog("save_photo_url", configure = {
            loggingData("vk_photo", vkPhoto)
            loggingData("photo_url", photoUrl)
        }) {
            transaction {
                vkPhoto.photoUrl = photoUrl
                vkPhoto.state = VkPhotoState.PHOTO_URL_SAVED
            }
        }
    }
}