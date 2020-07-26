package ru.serobyan.vk_photo_crawler.utils.logging

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class OperationLoggerContext(
    val operation: Operation,
    val data: MutableMap<String, Any?> = ConcurrentHashMap(),
    val counters: MutableMap<String, AtomicLong> = ConcurrentHashMap()
)