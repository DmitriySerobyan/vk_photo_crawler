package ru.serobyan.vk_photo_crawler.app.arguments

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class ArgumentsParserTest: StringSpec({
    data class Case(
        val args: String,
        val parsedArgs: Arguments
    )

    val cases = listOf(
        Case(
            args = "vk_photo_crawler -c ud -g https://vk.com/academicart -l myLogin -p myPassword",
            parsedArgs = Arguments(
                setOf(AppCommand.CRAWL_PHOTO_URLS, AppCommand.DOWNLOAD_PHOTOS),
                password = "myPassword",
                vkGroupUrl = "https://vk.com/academicart",
                login = "myLogin"
            )
        )
    )

    cases.forEach { case ->
        "parse arguments `${case.args}` to `${case.parsedArgs}`" {
            val parsedArgs = ArgumentsParser.parse(args = case.args.split(" ").toTypedArray())
            parsedArgs shouldBe case.parsedArgs
        }
    }

})