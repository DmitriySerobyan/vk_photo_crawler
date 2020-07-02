package ru.serobyan.vk_photo_crawler.app

import org.kodein.di.Kodein
import org.kodein.di.generic.instance
import ru.serobyan.vk_photo_crawler.service.vk.VkGroupPhotoIdsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.VkGroupPhotoUrlsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.VkPhotoDownloader

object App {
    suspend fun run(arguments: Arguments, di: Kodein) {
        arguments.commands
            .sortedBy { it.priority }
            .forEach { command ->
                when (command) {
                    AppCommand.CRAWL_PHOTO_IDS -> {
                        val idsCrawler: VkGroupPhotoIdsCrawler by di.instance()
                        crawlPhotoIds(crawler = idsCrawler, arguments = arguments)
                    }
                    AppCommand.CRAWL_PHOTO_URLS -> {
                        val urlsCrawler: VkGroupPhotoUrlsCrawler by di.instance()
                        crawlPhotoUrls(crawler = urlsCrawler, arguments = arguments)
                    }
                    AppCommand.DOWNLOAD_PHOTOS -> {
                        val photoDownloader: VkPhotoDownloader by di.instance()
                        downloadPhotos(photoDownloader = photoDownloader)
                    }
                }
            }
    }

    private suspend fun crawlPhotoIds(
        crawler: VkGroupPhotoIdsCrawler,
        arguments: Arguments
    ) {
        crawler.crawlPhotoIds(
            groupUrl = arguments.groupUrl,
            login = arguments.login,
            password = arguments.password
        )
    }

    private suspend fun crawlPhotoUrls(
        crawler: VkGroupPhotoUrlsCrawler,
        arguments: Arguments
    ) {
        crawler.crawlPhotoUrls(
            login = arguments.login,
            password = arguments.password
        )
    }

    private suspend fun downloadPhotos(photoDownloader: VkPhotoDownloader) {
        photoDownloader.downloadPhotos()
    }
}