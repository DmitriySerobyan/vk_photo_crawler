package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoIdsGetterContext(
    val groupUrl: String,
    val logger: IOperationLogger? = null
)