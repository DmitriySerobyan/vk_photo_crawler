package ru.serobyan.vk_photo_crawler.app

import net.lightbody.bmp.BrowserMobProxy
import org.kodein.di.DI
import org.kodein.di.generic.instance
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.app.arguments.AppCommand
import ru.serobyan.vk_photo_crawler.app.arguments.Arguments
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawlerContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloader
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloaderContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawlerContext
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog

class App(
    val arguments: Arguments,
    val di: DI
) {
    suspend fun run() {
        operationLog("app_run") {
            arguments.commands
                .sortedBy { it.priority }
                .forEach { command ->
                    when (command) {
                        AppCommand.CRAWL_PHOTO_IDS -> crawlPhotoIds()
                        AppCommand.CRAWL_PHOTO_URLS -> crawlPhotoUrls()
                        AppCommand.DOWNLOAD_PHOTOS -> downloadPhotos()
                    }
                }
        }
    }

    suspend fun finish() {
        operationLog("app_finish") {
            if (arguments.commands.intersect(setOf(AppCommand.CRAWL_PHOTO_IDS)).isNotEmpty()) {
                val browserMobProxy by di.instance<BrowserMobProxy>()
                browserMobProxy.stop()
                val webDriverWithProxy by di.instance<WebDriver>(tag = "proxy")
                webDriverWithProxy.quit()
            }
            if (arguments.commands.intersect(setOf(AppCommand.CRAWL_PHOTO_URLS)).isNotEmpty()) {
                val webDriver by di.instance<WebDriver>(tag = "no-proxy")
                webDriver.quit()
            }
        }
    }

    private suspend fun crawlPhotoIds() {
        val crawler: VkGroupPhotoIdsCrawler by di.instance()
        crawler.crawlPhotoIds(VkGroupPhotoIdsCrawlerContext(
            groupUrl = arguments.groupUrl,
            login = arguments.login,
            password = arguments.password
        ))
    }

    private suspend fun crawlPhotoUrls() {
        val crawler: VkGroupPhotoUrlsCrawler by di.instance()
        crawler.crawlPhotoUrls(
            VkGroupPhotoUrlsCrawlerContext(
                login = arguments.login,
                password = arguments.password,
                groupUrl = arguments.groupUrl
            )
        )
    }

    private suspend fun downloadPhotos() {
        val photoDownloader: VkPhotoDownloader by di.instance()
        photoDownloader.downloadPhotos(VkPhotoDownloaderContext())
    }
}