package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoUrlsCrawlerContext(
    val groupUrl: String,
    val login: String,
    val password: String,
    val logger: IOperationLogger
)