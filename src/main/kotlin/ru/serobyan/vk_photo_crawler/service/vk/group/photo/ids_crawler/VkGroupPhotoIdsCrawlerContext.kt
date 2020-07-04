package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoIdsCrawlerContext(
    val groupUrl: String,
    val login: String,
    val password: String
) {
    lateinit var operationLogger: IOperationLogger
}