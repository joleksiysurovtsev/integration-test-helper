package dev.surovtsev.integration.entity

import org.springframework.kafka.support.KafkaHeaders
import kotlin.reflect.full.declaredMemberProperties

/**
 * Converts `KafkaHeaders` to a `Map<String, String>`, extracting all non-null header values.
 *
 * ### Kafka Headers Mapping:
 *
 * | **Header**                        | **Description** |
 * |------------------------------------|----------------|
 * | `KafkaHeaders.TOPIC`              | Topic name. |
 * | `KafkaHeaders.PARTITION`          | Partition number where the message is sent or received. |
 * | `KafkaHeaders.OFFSET`             | Message offset in the partition. |
 * | `KafkaHeaders.MESSAGE_KEY`        | Message key (if using key-value producer). |
 * | `KafkaHeaders.TIMESTAMP`          | Timestamp of message creation. |
 * | `KafkaHeaders.RECEIVED_TOPIC`     | Topic from which the message was received. |
 * | `KafkaHeaders.RECEIVED_PARTITION` | Partition from which the message was received. |
 * | `KafkaHeaders.RECEIVED_OFFSET`    | Offset of the received message in the partition. |
 * | `KafkaHeaders.RECEIVED_TIMESTAMP` | Timestamp of message reception. |
 * | `KafkaHeaders.RECEIVED_KEY`       | Key of the received message. |
 * | `KafkaHeaders.GROUP_ID`           | Consumer group ID. |
 * | `KafkaHeaders.ACKNOWLEDGMENT`     | Acknowledgment actor for explicit message processing confirmation. |
 * | `KafkaHeaders.CORRELATION_ID`     | Correlation ID for request-response pattern. |
 * | `KafkaHeaders.REPLY_TOPIC`        | Reply topic (used in request-reply scenarios). |
 * | `KafkaHeaders.REPLY_PARTITION`    | Reply partition. |
 *
 * @return A map of Kafka headers with their corresponding values as strings.
 */
fun KafkaHeaders.toMap(): Map<String, String>{
		return this::class.declaredMemberProperties.mapNotNull { member ->
			if (member.getter.call(this) != null) {
				member.name to member.getter.call(this).toString()
			}
			else null
		}.toMap()
	}