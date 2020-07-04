package ru.serobyan.vk_photo_crawler.model

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object VkPhotoTable : IntIdTable() {
    override val tableName = "vk_photo"
    val groupUrl = varchar("group_url", 255)
    val photoId = varchar("group_photo_id", 255)
    val photoUrl = varchar("photo_url", 255).nullable()
    val state = enumerationByName("state", 255, VkPhotoState::class)
    init {
        index(true, groupUrl, photoId)
    }
}

class VkPhoto(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<VkPhoto>(VkPhotoTable)

    var groupUrl by VkPhotoTable.groupUrl
    var photoId by VkPhotoTable.photoId
    var photoUrl by VkPhotoTable.photoUrl
    var state by VkPhotoTable.state
}

enum class VkPhotoState {
    PHOTO_ID_SAVED,
    PHOTO_URL_SAVED,
    PHOTO_URL_ERROR,
    DOWNLOADED,
    DOWNLOAD_ERROR,
}