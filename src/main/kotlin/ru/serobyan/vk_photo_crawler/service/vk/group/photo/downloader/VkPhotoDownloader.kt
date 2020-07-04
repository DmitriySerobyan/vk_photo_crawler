package ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.io.FilenameUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import ru.serobyan.vk_photo_crawler.di.Config
import ru.serobyan.vk_photo_crawler.model.VkPhotoEntity
import ru.serobyan.vk_photo_crawler.model.VkPhotoState
import ru.serobyan.vk_photo_crawler.model.VkPhotoTable
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import java.io.File
import java.net.URL

class VkPhotoDownloader(
    private val photosDir: String = Config.photosDir,
    private val parallelPhotoDownloadCount: Int = Runtime.getRuntime().availableProcessors() * 4
) {
    suspend fun downloadPhotos(context: VkPhotoDownloaderContext) {
        operationLog("download_photos", configure = {
            loggingData("photos_dir", photosDir)
            loggingData("parallel_photo_download_count", parallelPhotoDownloadCount)
        }) {
            context.operationLogger = this
            runBlocking(Dispatchers.IO) {
                val vkPhotos = produce {
                    while (true) {
                        val vkPhotos = context.getVkPhotos()
                        if (vkPhotos.isEmpty()) break
                        incrementCounter("read_vk_photos", vkPhotos.size.toLong())
                        vkPhotos.forEach { send(it) }
                    }
                }
                repeat(parallelPhotoDownloadCount) {
                    launch {
                        for (vkPhoto in vkPhotos) {
                            context.savePhotoInFile(url = vkPhoto.photoUrl!!)
                            context.markDownloaded(vkPhoto = vkPhoto)
                            incrementCounter("saved_vk_photos")
                        }
                    }
                }
            }
        }
    }

    private suspend fun VkPhotoDownloaderContext.getVkPhotos(): List<VkPhotoEntity> {
        return operationLogger.subOperationLog("get_vk_photos") {
            val vkPhotos = transaction {
                VkPhotoEntity
                    .find { (VkPhotoTable.state eq VkPhotoState.PHOTO_URL_SAVED) and (VkPhotoTable.photoUrl.isNotNull()) }
                    .limit(1000)
                    .toList()
            }
            loggingData("vk_photos", vkPhotos.map { it.toVkPhoto() })
            vkPhotos
        }
    }

    private suspend fun VkPhotoDownloaderContext.savePhotoInFile(url: String) {
        operationLogger.subOperationLog("save_photo_in_file", configure = {
            loggingData("url", url)
        }) {
            val photoContent = getPhotoContent(url = url)
            val photoFileName = FilenameUtils.getName(URL(url).path)
            loggingData("photo_file_name", photoFileName)
            val file = File(photosDir, photoFileName)
            file.writeBytes(photoContent)
        }
    }

    private suspend fun VkPhotoDownloaderContext.markDownloaded(vkPhoto: VkPhotoEntity) {
        operationLogger.subOperationLog("mark_downloaded") {
            transaction {
                vkPhoto.state = VkPhotoState.DOWNLOADED
            }
        }
    }

    private suspend fun VkPhotoDownloaderContext.getPhotoContent(url: String): ByteArray {
        return operationLogger.subOperationLog("get_photo_content", configure = {
            loggingData("url", url)
        }) {
            createClient().use { client -> client.get<ByteArray>(url) }
        }
    }

    private fun createClient(): HttpClient {
        return HttpClient(Apache) {}
    }
}