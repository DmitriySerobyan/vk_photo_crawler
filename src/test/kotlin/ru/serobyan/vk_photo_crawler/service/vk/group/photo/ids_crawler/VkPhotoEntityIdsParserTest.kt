package ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize

class VkPhotoEntityIdsParserTest : StringSpec({

    "parse group main page" {
        val html = this::class.java.classLoader.getResourceAsStream("groupMainPage.html")!!.readAllBytes()
            .toString(Charsets.UTF_8)
        val photoIds = VkPhotoIdsParser.parseGroupMainPage(html = html)
        photoIds shouldHaveSize 17
    }

    "parse more post response" {
        val html = this::class.java.classLoader.getResourceAsStream("morePostResponse.html")!!.readAllBytes()
            .toString(Charsets.UTF_8)
        val photoIds = VkPhotoIdsParser.parseMorePostResponse(html = html)
        photoIds shouldHaveSize 57
    }
    
    "parse post" {
        val html = this::class.java.classLoader.getResourceAsStream("postFromAnotherGroupWithManyPhoto.html")!!.readAllBytes()
            .toString(Charsets.UTF_8)
        val photoIds = VkPhotoIdsParser.parsePost(html = html)
        assertSoftly {
            photoIds shouldContain "-56550760_457323841"
            photoIds shouldHaveSize 6
        }
    }

})