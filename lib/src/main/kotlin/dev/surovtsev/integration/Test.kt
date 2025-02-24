package dev.surovtsev.integration

import dev.surovtsev.integration.entity.KafkaEvent
import dev.surovtsev.integration.util.JsonUtil
import dev.surovtsev.integration.util.KafkaProducer
import java.util.*

class Test {

    fun test(){

       val testBody =  mapOf<String, String>("test" to  "test")

        KafkaProducer.sendMessages(
            kafkaEventActionType = KafkaEvent.TEST,
            body = JsonUtil.getAsString(testBody),
            UUID.randomUUID(),
            mutableMapOf("actionId" to UUID.randomUUID().toString())
        )
    }
}