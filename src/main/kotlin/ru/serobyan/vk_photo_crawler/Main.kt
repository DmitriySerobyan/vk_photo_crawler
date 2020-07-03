package ru.serobyan.vk_photo_crawler

import kotlinx.coroutines.runBlocking
import net.lightbody.bmp.BrowserMobProxy
import org.apache.commons.cli.ParseException
import org.kodein.di.generic.instance
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.app.App
import ru.serobyan.vk_photo_crawler.app.arguments.ArgumentsParser
import ru.serobyan.vk_photo_crawler.di.di
import ru.serobyan.vk_photo_crawler.utils.json.Json
import kotlin.system.exitProcess

fun main(args: Array<String>) = runBlocking {
    try {
        val arguments = ArgumentsParser.parse(args = args)
        App.run(arguments = arguments, di = di)
    } catch (e: ParseException) {
        exitProcess(1)
    } catch (e: Throwable) {
        println(Json.toJson(e, pretty = true))
    } finally {
        val webDriver by di.instance<WebDriver>(tag = "no-proxy")
        webDriver.quit()
        val webDriverWithProxy by di.instance<WebDriver>(tag = "proxy")
        webDriverWithProxy.quit()
        val browserMobProxy by di.instance<BrowserMobProxy>()
        browserMobProxy.stop()
    }
}