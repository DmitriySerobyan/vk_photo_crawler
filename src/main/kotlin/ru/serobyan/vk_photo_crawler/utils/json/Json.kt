package ru.serobyan.vk_photo_crawler.utils.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal val gson: Gson by lazy { GsonBuilder().create() }
internal val gsonPretty: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }

inline fun <reified T> convert(from: Any): T {
    val json = from.toJSON()
    return fromJSON(json, T::class.java)
}

fun Any?.toJSON(pretty: Boolean = false): String {
    return (if (pretty) gsonPretty else gson).toJson(this)
}

inline fun <reified T> fromJSON(json: String): T {
    return fromJSON(json, T::class.java)
}

fun <T> fromJSON(json: String, clazz: Class<T>): T {
    return gson.fromJson(json, clazz)
}