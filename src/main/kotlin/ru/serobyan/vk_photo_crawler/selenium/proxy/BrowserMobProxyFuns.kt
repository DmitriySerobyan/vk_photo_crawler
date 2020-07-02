package ru.serobyan.vk_photo_crawler.selenium.proxy

import net.lightbody.bmp.BrowserMobProxy
import net.lightbody.bmp.core.har.HarEntry

fun BrowserMobProxy.getHarEntryByUrl(url: String): List<HarEntry> {
    return this.har.log.entries.filter { it.request.url == url }
}