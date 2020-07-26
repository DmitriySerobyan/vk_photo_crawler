package ru.serobyan.vk_photo_crawler.service.vk.login

import org.openqa.selenium.By
import org.openqa.selenium.Cookie
import org.openqa.selenium.WebDriver
import ru.serobyan.vk_photo_crawler.selenium.getElement
import ru.serobyan.vk_photo_crawler.selenium.waitUntil
import ru.serobyan.vk_photo_crawler.service.vk.cookie.CookieStorage
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog

class VkLoginService(
    private val driver: WebDriver,
    private val cookieStorage: CookieStorage
) {
    suspend fun login(context: VkLoginServiceContext) {
        context.logger.operationLog("login", configure = { put("login", context.login) }) { logger ->
            setCookiesFromStorage(logger = logger)
            goToMainPage(logger = logger)
            if (!isLogin(logger = logger)) {
                fillAndSubmitLoginForm(logger = logger, login = context.login, password = context.password)
                driver.waitUntil { isLogin(logger = logger) }
                val cookies = getFreshCookies(logger = logger)
                cookieStorage.save(cookies)
            }
        }
    }

    private suspend fun setCookiesFromStorage(logger: IOperationLogger) {
        logger.operationLog("set_cookies_from_storage", configure = { put("start_url", startUrl) }) { subLogger ->
            driver.get(startUrl)
            val storageCookies = cookieStorage.read()
            subLogger.put("storage_cookies", storageCookies)
            storageCookies.forEach { driver.manage().addCookie(it) }
        }
    }

    private suspend fun goToMainPage(logger: IOperationLogger) {
        logger.operationLog("go_to_main_page") {
            driver.get(mainPage)
        }
    }

    private suspend fun isLogin(logger: IOperationLogger): Boolean {
        return logger.operationLog("is_login") { subLogger ->
            val isLogin = "Моя страница" in driver.pageSource
            subLogger.put("is_login", isLogin)
            isLogin
        }
    }

    private suspend fun fillAndSubmitLoginForm(logger: IOperationLogger, login: String, password: String) {
        logger.operationLog("fill_and_submit_login_form") {
            val loginInput = driver.getElement(loginInputBy)
            val passwordInput = driver.getElement(passwordInputBy)
            val submitButton = driver.getElement(submitButtonBy)
            loginInput.sendKeys(login)
            passwordInput.sendKeys(password)
            submitButton.click()
        }
    }

    private suspend fun getFreshCookies(logger: IOperationLogger): Set<Cookie> {
        return logger.operationLog("get_fresh_cookies") { subLogger ->
            val freshCookies = driver.manage().cookies
            subLogger.put("fresh_cookies", freshCookies)
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