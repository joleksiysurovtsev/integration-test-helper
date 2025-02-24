package dev.surovtsev.integration.util

import dev.surovtsev.integration.container.KafkaContainer
import dev.surovtsev.integration.entity.KafkaEvent
import org.apache.kafka.clients.producer.*
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

object KafkaProducer {

	private fun createProducer(): KafkaProducer<String, String> {
		return KafkaProducer(getProperties())
	}

	private fun getProperties(): Properties {
		return Properties().apply {
			this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = KafkaContainer.getBootstrap()
			this[ProducerConfig.CLIENT_ID_CONFIG] = "intTest-${UUID.randomUUID()}"
			this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
			this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
		}
	}

	fun sendMessages(kafkaEventActionType: KafkaEvent, body: String, actionId:UUID? = UUID.randomUUID(), customHeaders: MutableMap<String, String>? = null): UUID? {
		val producer = createProducer()
		val callback = TestCallback()
		val recordHeaders = RecordHeaders()

		customHeaders?.toMap()?.forEach { (key, value) -> recordHeaders.add(key, value.toByteArray()) }
		recordHeaders.add("actionType", kafkaEventActionType.eventType.toByteArray())
		recordHeaders.add("contentType", "application/json".toByteArray())
        recordHeaders.add("actionId", actionId.toString().toByteArray() )

		val data = ProducerRecord(kafkaEventActionType.topicName, null, actionId.toString(), body, recordHeaders)
		producer.send(data, callback)
		producer.close()
        return actionId
	}

	private class TestCallback : Callback {
		override fun onCompletion(recordMetadata: RecordMetadata, e: Exception?) {
			if (e != null) {
				println("Error while producing message to topic: $recordMetadata")
				e.printStackTrace()
			} else {
				val message = String.format("Successfully sent message to topic: ${recordMetadata.topic()}")
				println(message)
			}
		}
	}
}
