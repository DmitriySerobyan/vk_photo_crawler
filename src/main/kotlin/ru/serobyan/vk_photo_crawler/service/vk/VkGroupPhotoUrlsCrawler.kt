package ru.serobyan.vk_photo_crawler.service.vk

import org.jetbrains.exposed.sql.transactions.transaction
import org.openqa.selenium.TimeoutException
import ru.serobyan.vk_photo_crawler.model.VkPhoto
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
                .find { VkPhotoTable.photoUrl.isNull() }
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
            }
        } catch (_: TimeoutException) {
        }
    }
}