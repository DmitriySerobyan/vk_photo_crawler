package ru.serobyan.vk_photo_crawler.app

import net.lightbody.bmp.BrowserMobProxy
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.app.arguments.AppCommand
import ru.serobyan.vk_photo_crawler.app.arguments.Arguments
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloader
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloaderContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawlerContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawlerContext
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import java.io.Closeable

class App(
    private val vkGroupPhotoIdsCrawler: VkGroupPhotoIdsCrawler,
    private val vkGroupPhotoUrlsCrawler: VkGroupPhotoUrlsCrawler,
    private val vkPhotoDownloader: VkPhotoDownloader,
    private val driverWithProxy: WebDriver,
    private val driver: WebDriver,
    private val proxy: BrowserMobProxy
) : Closeable {
    suspend fun run(
        logger: IOperationLogger,
        arguments: Arguments
    ) {
        logger.subOperationLog("app.run") { subLogger ->
            arguments.commands
                .sortedBy { it.priority }
                .forEach { command ->
                    when (command) {
                        AppCommand.CRAWL_PHOTO_IDS -> vkGroupPhotoIdsCrawler.crawlPhotoIds(
                            VkGroupPhotoIdsCrawlerContext(
                                groupUrl = arguments.groupUrl,
                                login = arguments.login,
                                password = arguments.password,
                                logger = subLogger
                            )
                        )
                        AppCommand.CRAWL_PHOTO_URLS -> vkGroupPhotoUrlsCrawler.crawlPhotoUrls(
                            VkGroupPhotoUrlsCrawlerContext(
                                login = arguments.login,
                                password = arguments.password,
                                groupUrl = arguments.groupUrl,
                                logger = subLogger
                            )
                        )
                        AppCommand.DOWNLOAD_PHOTOS ->
                            vkPhotoDownloader.downloadPhotos(
                                VkPhotoDownloaderContext(
                                    logger = subLogger
                                )
                            )
                    }
                }
        }
    }

    override fun close() {
        driverWithProxy.quit()
        driver.quit()
        proxy.stop()
    }
}