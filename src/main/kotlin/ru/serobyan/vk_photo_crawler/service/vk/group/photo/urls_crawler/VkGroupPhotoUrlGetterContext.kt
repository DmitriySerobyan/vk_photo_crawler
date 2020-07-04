package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoUrlGetterContext(
    val operationLogger: IOperationLogger,
    val groupUrl: String,
    val photoId: String
)