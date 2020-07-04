package ru.serobyan.vk_photo_crawler.utils.logging

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class LogContext(
    val operation: Operation = Operation(),
    val data: MutableMap<String, Any?> = ConcurrentHashMap(),
    val counters: MutableMap<String, AtomicLong> = ConcurrentHashMap(),
    var time: Long = Instant.now().epochSecond
)