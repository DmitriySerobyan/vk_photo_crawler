package ru.serobyan.vk_photo_crawler.service.vk

import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntil
import ru.serobyan.vk_photo_crawler.service.CookieStorage

class VkLoginService(
    private val driver: WebDriver,
    private val cookieStorage: CookieStorage
) {
    fun login(login: String, password: String) {
        setCookiesFromStorage()
        goToMainPage()
        if (!isLogin()) {
            fillAndSubmitLoginForm(login = login, password = password)
            driver.waitUntil { isLogin() }
            val cookies = getCookies()
            cookieStorage.save(cookies)
        }
    }

    private fun setCookiesFromStorage() {
        driver.get("https://vk.com/")
        val storageCookies = cookieStorage.read()
        storageCookies.forEach { driver.manage().addCookie(it) }
    }

    private fun goToMainPage() {
        driver.get("https://vk.com/")
    }

    private fun isLogin() = "Моя страница" in driver.pageSource

    private fun fillAndSubmitLoginForm(login: String, password: String) {
        val loginInput = driver.getElement(loginInputBy)
        val passwordInput = driver.getElement(passwordInputBy)
        val submitButton = driver.getElement(submitButtonBy)
        loginInput.sendKeys(login)
        passwordInput.sendKeys(password)
        submitButton.click()
    }

    private fun getCookies(): Set<Cookie> {
        return driver.manage().cookies
    }

    companion object {
        val loginInputBy: By = By.id("index_email")
        val passwordInputBy: By = By.id("index_pass")
        val submitButtonBy: By = By.id("index_login_button")
    }
}