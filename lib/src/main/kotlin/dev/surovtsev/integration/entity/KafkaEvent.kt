package dev.surovtsev.integration.entity

import org.json.JSONArray
import kotlin.reflect.KClass

enum class KafkaEvent(val eventType: String, val topicName: String, val kClass: KClass<*>, val messageOriginator: String? = null) {
    TEST("TEST", "EVENT_TOPIC", JSONArray::class, "test"),
}
