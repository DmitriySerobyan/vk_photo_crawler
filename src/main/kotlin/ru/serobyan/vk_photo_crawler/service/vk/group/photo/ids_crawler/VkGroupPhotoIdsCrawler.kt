package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.model.VkPhotoEntity
import ru.serobyan.vk_photo_crawler.model.VkPhotoState
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginService
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginServiceContext
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog

class VkGroupPhotoIdsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoIdsGetter: VkGroupPhotoIdsGetter
) {
    suspend fun crawlPhotoIds(context: VkGroupPhotoIdsCrawlerContext) {
        context.logger.subOrRootOperationLog("crawl_vk_group_photo_ids", configure = {
            put("login", context.login)
            put("group_url", context.groupUrl)
        }) { logger ->
            vkLoginService.login(
                VkLoginServiceContext(
                    logger = context.logger,
                    login = context.login,
                    password = context.password
                )
            )
            val photoIds = vkGroupPhotoIdsGetter.getPhotoIds(
                VkGroupPhotoIdsGetterContext(
                    logger = context.logger,
                    groupUrl = context.groupUrl
                )
            )
            photoIds.take(context.photoLimit).collect { photoId ->
                saveVkPhotoIdAndGroupUrl(logger = logger, photoId = photoId, groupUrl = context.groupUrl)
                logger.inc("crawled_photo_ids")
            }
        }
    }

    private suspend fun saveVkPhotoIdAndGroupUrl(logger: IOperationLogger, photoId: String, groupUrl: String) {
        logger.subOperationLog("save_vk_photo_id_and_group_url", configure = { put("photo_id", photoId) }) { subLogger ->
            try {
                transaction {
                    VkPhotoEntity.new {
                        this.photoId = photoId
                        this.groupUrl = groupUrl
                        this.state = VkPhotoState.PHOTO_ID_SAVED
                    }
                }
            } catch (_: ExposedSQLException) {
                subLogger.put("already_saved", true)
                subLogger.log(Level.WARN, "already saved")
            }
        }
    }
}