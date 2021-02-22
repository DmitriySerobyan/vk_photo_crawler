package ru.serobyan.vk_photo_crawler.di

object Config {
    const val pathToCookieStorage = ".cookies.txt"
    const val pathToChromeDriver: String = "chromedriver.exe"
    const val jdbcUrl = "jdbc:sqlite:.vk_photo_crawler.db"
    const val photosDir = "D:\\PicturesVk"
    const val vkTimeout: Long = 10L
    const val minDelayBetweenOpenPhotoPost: Long = 500L
    const val maxDelayBetweenOpenPhotoPost: Long = 1000L
    const val scrollDelay: Long = 10_000L
    const val vkMorePostRequestUrl = "https://vk.com/al_wall.php"
}