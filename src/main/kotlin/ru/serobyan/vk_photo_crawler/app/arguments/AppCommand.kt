package ru.serobyan.vk_photo_crawler.app.arguments

enum class AppCommand(
    val priority: Int,
    val consoleCommand: String,
    val description: String
) {
    CRAWL_PHOTO_IDS(
        priority = 1,
        consoleCommand = "i",
        description = "Crawl and save vk photo post ids"
    ),
    CRAWL_PHOTO_URLS(
        priority = 2,
        consoleCommand = "u",
        description = "Crawl photo urls from vk photo post ids and save it"
    ),
    DOWNLOAD_PHOTOS(
        priority = 3,
        consoleCommand = "d",
        description = "Download photos by photo urls"
    );

    companion object {
        fun fromConsoleCommand(consoleCommand: String): AppCommand {
            return values()
                .firstOrNull { it.consoleCommand == consoleCommand.toLowerCase() }
                ?: throw IllegalStateException("Unknown console command: $consoleCommand")
        }

        val availableConsoleCommands =
            "Available commands: ${values().joinToString("; ") { "${it.consoleCommand} - ${it.description}" }}"
    }
}