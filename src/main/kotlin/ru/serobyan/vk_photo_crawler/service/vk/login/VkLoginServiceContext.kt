package ru.serobyan.vk_photo_crawler.service.vk.login

import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger

data class VkLoginServiceContext(
    val login: String,
    val password: String,
    val logger: IOperationLogger? = null
)