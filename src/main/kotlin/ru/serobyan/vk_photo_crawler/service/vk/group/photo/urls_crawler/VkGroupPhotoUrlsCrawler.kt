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
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog

class VkGroupPhotoUrlsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoUrlGetter: VkGroupPhotoUrlGetter
) {
    suspend fun crawlPhotoUrls(context: VkGroupPhotoUrlsCrawlerContext) {
        context.logger.subOrRootOperationLog("crawl_vk_group_photo_urls", configure = {
            put("login", context.login)
            put("group_url", context.groupUrl)
        }) { logger ->
            vkLoginService.login(
                VkLoginServiceContext(
                    logger = logger,
                    login = context.login,
                    password = context.password
                )
            )
            while (true) {
                val vkPhotos = getVkPhotos(logger = logger, groupUrl = context.groupUrl)
                if (vkPhotos.isEmpty()) break
                vkPhotos.forEach { vkPhoto ->
                    val photoUrl = getPhotoUrl(logger = logger, vkPhoto = vkPhoto)
                    if (photoUrl == null) {
                        setErrorState(logger = logger, vkPhoto = vkPhoto)
                    } else {
                        savePhotoUrl(logger = logger, vkPhoto = vkPhoto, photoUrl = photoUrl)
                    }
                }
            }
        }
    }

    private suspend fun getVkPhotos(logger: IOperationLogger, groupUrl: String): List<VkPhotoEntity> {
        return logger.subOperationLog("get_vk_photos") { subLogger ->
            val vkPhotos = transaction {
                VkPhotoEntity
                    .find { (VkPhotoTable.state eq VkPhotoState.PHOTO_ID_SAVED) and (VkPhotoTable.groupUrl eq groupUrl) }
                    .limit(100)
                    .toList()
            }
            subLogger.put("vk_photos", vkPhotos.map { it.toVkPhoto() })
            vkPhotos
        }
    }

    private suspend fun getPhotoUrl(logger: IOperationLogger, vkPhoto: VkPhotoEntity): String? {
        return logger.subOperationLog("get_photo_url", configure = {
            put("vk_photo", vkPhoto.toVkPhoto())
        }) { subLogger ->
            try {
                val photoUrl = vkGroupPhotoUrlGetter.getPhotoUrl(
                    VkGroupPhotoUrlGetterContext(
                        logger = subLogger,
                        groupUrl = vkPhoto.groupUrl,
                        photoId = vkPhoto.photoId
                    )
                )
                subLogger.put("photo_url", photoUrl)
                photoUrl
            } catch (_: TimeoutException) {
                subLogger.put("photo_url_received", true)
                subLogger.log(Level.WARN, "failed to get photo_url")
                null
            }
        }
    }

    private suspend fun setErrorState(logger: IOperationLogger, vkPhoto: VkPhotoEntity) {
        logger.subOperationLog("set_error_state", configure = {
            put("vk_photo", vkPhoto.toVkPhoto())
        }) {
            transaction {
                vkPhoto.state = VkPhotoState.PHOTO_URL_ERROR
            }
        }
    }

    private suspend fun savePhotoUrl(logger: IOperationLogger, vkPhoto: VkPhotoEntity, photoUrl: String) {
        logger.subOperationLog("save_photo_url", configure = {
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