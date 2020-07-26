package ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoUrlGetterContext(
    val groupUrl: String,
    val photoId: String,
    val logger: IOperationLogger? = null
)