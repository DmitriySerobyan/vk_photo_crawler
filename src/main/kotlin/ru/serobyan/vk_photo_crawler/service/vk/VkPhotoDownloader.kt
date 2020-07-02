package ru.serobyan.vk_photo_crawler.service.vk

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import org.apache.commons.io.FilenameUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.model.VkPhoto
import ru.serobyan.vk_photo_crawler.model.VkPhotoTable
import java.io.File
import java.net.URL

class VkPhotoDownloader(
    private val photosDir: String = Config.photosDir
) {
    suspend fun downloadPhotos() {
        while (true) {
            val vkPhotos = getVkPhotos()
            if(vkPhotos.isEmpty()) break
            vkPhotos.forEach { vkPhoto ->
                savePhotoInFile(url = vkPhoto.photoUrl!!)
                markDownloaded(vkPhoto)
            }
        }
    }

    private fun getVkPhotos(): List<VkPhoto> {
        return transaction {
            VkPhoto
                .find { (VkPhotoTable.isDownloaded eq false) and (VkPhotoTable.photoUrl.isNotNull()) }
                .limit(100)
                .toList()
        }
    }

    private suspend fun savePhotoInFile(url: String) {
        val photoContent = getPhotoContent(url = url)
        val photoFileName = FilenameUtils.getName(URL(url).path)
        val file = File(photosDir, photoFileName)
        file.writeBytes(photoContent)
    }

    private fun markDownloaded(vkPhoto: VkPhoto) {
        transaction {
            vkPhoto.isDownloaded = true
        }
    }

    private suspend fun getPhotoContent(url: String): ByteArray {
        return createClient().use { client -> client.get(url) }
    }

    private fun createClient(): HttpClient {
        return HttpClient(Apache) {}
    }
}