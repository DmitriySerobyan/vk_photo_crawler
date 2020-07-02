package ru.serobyan.vk_photo_crawler.selenium

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import ru.serobyan.vk_photo_crawler.di.Config

fun WebDriver.getElement(selector: By, timeout: Long = Config.vkTimeout): WebElement {
    return WebDriverWait(this, timeout)
        .until(ExpectedConditions.presenceOfElementLocated(selector))
}

fun WebDriver.waitUntil(timeout: Long = Config.vkTimeout, checker: WebDriver.() -> Boolean) {
    WebDriverWait(this, timeout)
        .until { checker() }
}

fun WebDriver.waitUntilVisibility(selector: By, timeout: Long = Config.vkTimeout) {
    WebDriverWait(this, timeout)
        .until(ExpectedConditions.visibilityOfElementLocated(selector))
}

fun WebDriver.scrollBy(x: Int = 0, y: Int = 10000) {
    (this as JavascriptExecutor).executeScript("window.scrollBy(${x}, ${y})")
}

fun WebDriver.alert(message: String) {
    (this as JavascriptExecutor).executeScript("alert(`$message`)")
}