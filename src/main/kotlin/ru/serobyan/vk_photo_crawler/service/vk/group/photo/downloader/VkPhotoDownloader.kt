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
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog
import java.io.File
import java.net.URL

class VkPhotoDownloader(
    private val photosDir: String = Config.photosDir,
    private val parallelPhotoDownloadCount: Int = Runtime.getRuntime().availableProcessors() * 4
) {
    suspend fun downloadPhotos(context: VkPhotoDownloaderContext) {
        context.logger.subOrRootOperationLog("download_photos", configure = {
            put("photos_dir", photosDir)
            put("parallel_photo_download_count", parallelPhotoDownloadCount)
        }) { logger ->
            runBlocking(Dispatchers.IO) {
                val vkPhotos = produce {
                    while (true) {
                        val vkPhotos = getVkPhotos(logger = logger)
                        if (vkPhotos.isEmpty()) break
                        logger.inc("read_vk_photos", vkPhotos.size.toLong())
                        vkPhotos.forEach { send(it) }
                    }
                }
                repeat(parallelPhotoDownloadCount) {
                    launch {
                        for (vkPhoto in vkPhotos) {
                            savePhotoInFile(logger = logger, url = vkPhoto.photoUrl!!)
                            markDownloaded(logger = logger, vkPhoto = vkPhoto)
                            logger.inc("saved_vk_photos")
                        }
                    }
                }
            }
        }
    }

    private suspend fun getVkPhotos(logger: IOperationLogger): List<VkPhotoEntity> {
        return logger.subOperationLog("get_vk_photos") {
            val vkPhotos = transaction {
                VkPhotoEntity
                    .find { (VkPhotoTable.state eq VkPhotoState.PHOTO_URL_SAVED) and (VkPhotoTable.photoUrl.isNotNull()) }
                    .limit(1000)
                    .toList()
            }
            logger.put("vk_photos", vkPhotos.map { it.toVkPhoto() })
            vkPhotos
        }
    }

    private suspend fun savePhotoInFile(logger: IOperationLogger, url: String) {
        logger.subOperationLog("save_photo_in_file", configure = { put("url", url) }) {
            val photoContent = getPhotoContent(url = url, logger = logger)
            val photoFileName = FilenameUtils.getName(URL(url).path)
            logger.put("photo_file_name", photoFileName)
            val file = File(photosDir, photoFileName)
            file.writeBytes(photoContent)
        }
    }

    private suspend fun markDownloaded(logger: IOperationLogger, vkPhoto: VkPhotoEntity) {
        logger.subOperationLog("mark_downloaded") {
            transaction {
                vkPhoto.state = VkPhotoState.DOWNLOADED
            }
        }
    }

    private suspend fun getPhotoContent(logger: IOperationLogger, url: String): ByteArray {
        return logger.subOperationLog("get_photo_content", configure = {
            put("url", url)
        }) {
            createClient().use { client -> client.get<ByteArray>(url) }
        }
    }

    private fun createClient(): HttpClient {
        return HttpClient(Apache) {}
    }
}