package ru.serobyan.vk_photo_crawler.service.vk

import org.jetbrains.exposed.sql.transactions.transaction
import org.openqa.selenium.TimeoutException
import ru.serobyan.vk_photo_crawler.model.VkPhoto
import ru.serobyan.vk_photo_crawler.model.VkPhotoState
import ru.serobyan.vk_photo_crawler.model.VkPhotoTable

class VkGroupPhotoUrlsCrawler(
    private val vkLoginService: VkLoginService,
    private val vkGroupPhotoUrlGetter: VkGroupPhotoUrlGetter
) {
    suspend fun crawlPhotoUrls(
        login: String,
        password: String
    ) {
        vkLoginService.login(login = login, password = password)
        while (true) {
            val vkPhotos = getVkPhotos()
            if (vkPhotos.isEmpty()) break
            vkPhotos.forEach { vkPhoto ->
                getAndSavePhotoUrl(vkPhoto = vkPhoto)
            }
        }
    }

    private fun getVkPhotos(): List<VkPhoto> {
        return transaction {
            VkPhoto
                .find { VkPhotoTable.state eq VkPhotoState.PHOTO_ID_SAVED }
                .limit(100)
                .toList()
        }
    }

    private suspend fun getAndSavePhotoUrl(vkPhoto: VkPhoto) {
        try {
            val photoUrl = vkGroupPhotoUrlGetter.getPhotoUrl(
                groupUrl = vkPhoto.groupUrl,
                photoId = vkPhoto.photoId
            )
            transaction {
                vkPhoto.photoUrl = photoUrl
                vkPhoto.state = VkPhotoState.PHOTO_URL_SAVED
            }
        } catch (_: TimeoutException) {
            transaction {
                vkPhoto.state = VkPhotoState.PHOTO_URL_ERROR
            }
        }
    }
}