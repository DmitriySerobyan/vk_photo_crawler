package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.openqa.selenium.TimeoutException
import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.model.VkPhotoEntity
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
            put("login", context.login)
            put("group_url", context.groupUrl)
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

    private suspend fun VkGroupPhotoUrlsCrawlerContext.getVkPhotos(): List<VkPhotoEntity> {
        return operationLogger.subOperationLog("get_vk_photos") {
            val vkPhotos = transaction {
                VkPhotoEntity
                    .find { (VkPhotoTable.state eq VkPhotoState.PHOTO_ID_SAVED) and (VkPhotoTable.groupUrl eq groupUrl) }
                    .limit(100)
                    .toList()
            }
            put("vk_photos", vkPhotos.map { it.toVkPhoto() })
            vkPhotos
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.getPhotoUrl(vkPhoto: VkPhotoEntity): String? {
        return operationLogger.subOperationLog("get_photo_url", configure = {
            put("vk_photo", vkPhoto.toVkPhoto())
        }) {
            try {
                val photoUrl = vkGroupPhotoUrlGetter.getPhotoUrl(
                    VkGroupPhotoUrlGetterContext(
                        operationLogger = this,
                        groupUrl = vkPhoto.groupUrl,
                        photoId = vkPhoto.photoId
                    )
                )
                put("photo_url", photoUrl)
                photoUrl
            } catch (_: TimeoutException) {
                put("photo_url_received", true)
                log(Level.WARN, "failed to get photo_url")
                null
            }
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.setErrorState(vkPhoto: VkPhotoEntity) {
        operationLogger.subOperationLog("set_error_state", configure = {
            put("vk_photo", vkPhoto.toVkPhoto())
        }) {
            transaction {
                vkPhoto.state = VkPhotoState.PHOTO_URL_ERROR
            }
        }
    }

    private suspend fun VkGroupPhotoUrlsCrawlerContext.savePhotoUrl(vkPhoto: VkPhotoEntity, photoUrl: String) {
        operationLogger.subOperationLog("save_photo_url", configure = {
            put("vk_photo", vkPhoto.toVkPhoto())
            put("photo_url", photoUrl)
        }) {
            transaction {
                vkPhoto.photoUrl = photoUrl
                vkPhoto.state = VkPhotoState.PHOTO_URL_SAVED
            }
        }
    }
}