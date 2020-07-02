package ru.serobyan.vk_photo_crawler.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object Json {
    private val gson: Gson by lazy { GsonBuilder().create() }
    private val gsonPretty: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }

    inline fun <reified T> fromJson(json: String): T {
        return fromJson(json, T::class.java)
    }

    fun <T> fromJson(json: String, clazz: Class<T>): T {
        return gson.fromJson(json, clazz)
    }

    fun toJson(obj: Any?, pretty: Boolean = false): String {
        return (if (pretty) gsonPretty else gson).toJson(obj)
    }

    inline fun <reified T> convert(source: Any): T {
        val json = toJson(source)
        return fromJson(json, T::class.java)
    }
}