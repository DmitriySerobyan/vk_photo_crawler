package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import kotlinx.coroutines.flow.collect
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level
import ru.serobyan.vk_photo_crawler.model.VkPhotoEntity
import ru.serobyan.vk_photo_crawler.model.VkPhotoState
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginService
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginServiceContext
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog

class VkGroupPhotoIdsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoIdsGetter: VkGroupPhotoIdsGetter
) {
    suspend fun crawlPhotoIds(context: VkGroupPhotoIdsCrawlerContext) {
        operationLog("crawl_vk_group_photo_ids", configure = {
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
            val photoIds = vkGroupPhotoIdsGetter.getPhotoIds(
                VkGroupPhotoIdsGetterContext(
                    operationLogger = context.operationLogger,
                    groupUrl = context.groupUrl
                )
            )
            photoIds.collect { photoId ->
                context.saveVkPhotoIdAndGroupUrl(photoId)
                incrementCounter("crawled_photo_ids")
            }
        }
    }

    private suspend fun VkGroupPhotoIdsCrawlerContext.saveVkPhotoIdAndGroupUrl(photoId: String) {
        operationLogger.subOperationLog("save_vk_photo_id_and_group_url", configure = {
            loggingData("photo_id", photoId)
            loggingData("group_url", groupUrl)
        }) {
            try {
                transaction {
                    VkPhotoEntity.new {
                        this.photoId = photoId
                        this.groupUrl = groupUrl
                        this.state = VkPhotoState.PHOTO_ID_SAVED
                    }
                }
            } catch (_: ExposedSQLException) {
                loggingData("already_saved", true)
                log(Level.WARN, "already saved")
            }
        }
    }
}