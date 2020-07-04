package ru.serobyan.vk_photo_crawler.service.vk

import kotlinx.coroutines.flow.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import ru.serobyan.vk_photo_crawler.model.VkPhoto
import ru.serobyan.vk_photo_crawler.model.VkPhotoState

class VkGroupPhotoIdsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoIdsGetter: VkGroupPhotoIdsGetter
) {
    suspend fun crawlPhotoIds(
        groupUrl: String,
        login: String,
        password: String
    ) {
        vkLoginService.login(login = login, password = password)
        val photoIds = vkGroupPhotoIdsGetter.getPhotoIds(groupUrl = groupUrl)
        photoIds.collect{ photoId ->
            saveVkPhotoIdAndGroupUrl(photoId, groupUrl)
        }
    }

    private fun saveVkPhotoIdAndGroupUrl(photoId: String, groupUrl: String) {
        try {
            transaction {
                VkPhoto.new {
                    this.photoId = photoId
                    this.groupUrl = groupUrl
                    this.state = VkPhotoState.PHOTO_ID_SAVED
                }
            }
        } catch (_: ExposedSQLException) {
        }
    }
}