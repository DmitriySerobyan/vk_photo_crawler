package ru.serobyan.vk_photo_crawler.service.vk.login

import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntil
import ru.serobyan.vk_photo_crawler.service.vk.cookie.CookieStorage
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog

class VkLoginService(
    private val driver: WebDriver,
    private val cookieStorage: CookieStorage
) {
    suspend fun login(context: VkLoginServiceContext) {
        context.operationLogger.subOperationLog("login", configure = {
            put("login", context.login)
        }) {
            context.setCookiesFromStorage()
            context.goToMainPage()
            if (!context.isLogin()) {
                context.fillAndSubmitLoginForm()
                driver.waitUntil { context.isLogin() }
                val cookies = context.getFreshCookies()
                cookieStorage.save(cookies)
            }
        }
    }

    private suspend fun VkLoginServiceContext.setCookiesFromStorage() {
        operationLogger.subOperationLog("set_cookies_from_storage", configure = {
            put("start_url", startUrl)
        }) {
            driver.get(startUrl)
            val storageCookies = cookieStorage.read()
            put("storage_cookies", storageCookies)
            storageCookies.forEach { driver.manage().addCookie(it) }
        }
    }

    private suspend fun VkLoginServiceContext.goToMainPage() {
        operationLogger.subOperationLog("go_to_main_page") {
            driver.get(mainPage)
        }
    }

    private suspend fun VkLoginServiceContext.isLogin(): Boolean {
        return operationLogger.subOperationLog("is_login") {
            val isLogin = "Моя страница" in driver.pageSource
            put("is_login", isLogin)
            isLogin
        }
    }

    private suspend fun VkLoginServiceContext.fillAndSubmitLoginForm() {
        operationLogger.subOperationLog("fill_and_submit_login_form") {
            val loginInput = driver.getElement(loginInputBy)
            val passwordInput = driver.getElement(passwordInputBy)
            val submitButton = driver.getElement(submitButtonBy)
            loginInput.sendKeys(login)
            passwordInput.sendKeys(password)
            submitButton.click()
        }
    }

    private suspend fun VkLoginServiceContext.getFreshCookies(): Set<Cookie> {
        return operationLogger.subOperationLog("get_fresh_cookies") {
            val freshCookies = driver.manage().cookies
            put("fresh_cookies", freshCookies)
            freshCookies
        }
    }

    companion object {
        const val startUrl = "https://vk.com/"
        const val mainPage = "https://vk.com/"
        val loginInputBy: By = By.id("index_email")
        val passwordInputBy: By = By.id("index_pass")
        val submitButtonBy: By = By.id("index_login_button")
    }
}