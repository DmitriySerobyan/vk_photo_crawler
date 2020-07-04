package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import org.jsoup.Jsoup

object VkPhotoIdsParser {

    fun parseGroupMainPage(html: String): List<String> {
        val document = Jsoup.parse(html)
        val posts = document.select(postSelector)
        return posts.flatMap { post ->
            parsePost(
                post.html()
            )
        }
    }

    fun parseMorePostResponse(html: String): List<String> {
        val document = Jsoup.parse(html)
        val posts = document.select(postSelector)
        return posts.flatMap { post ->
            parsePost(
                post.html()
            )
        }
    }

    fun parsePost(html: String): List<String> {
        val document = Jsoup.parse(html)
        val images = document.select(imageSelector)
        return images.map { it.attr(imageIdAttribute) }
    }

    private const val postSelector = ".post"
    private const val imageIdAttribute = "data-photo-id"
    private const val imageSelector = "a[$imageIdAttribute]"

}