package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkGroupPhotoIdsGetterContext(
    val operationLogger: IOperationLogger,
    val groupUrl: String
)