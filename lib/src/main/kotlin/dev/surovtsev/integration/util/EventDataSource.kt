package dev.surovtsev.integration.util


import dev.surovtsev.integration.container.KafkaContainer
import dev.surovtsev.integration.container.PostgresContainer
import dev.surovtsev.integration.entity.KafkaEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.support.KafkaHeaders
import java.time.Duration
import java.util.*

object EventDataSource {

	private val jdbcTemplate: JdbcTemplate
	private val topics = KafkaEvent.entries.map { it.topicName }.toSet()
	private val supportedEvents = KafkaEvent.entries.map { it.eventType }.toSet()

	init {
		val container: PostgresContainer = PostgresContainer.getInstance()
		val dbBuilder: DataSourceBuilder<*> = DataSourceBuilder.create()
		dbBuilder.url(container.jdbcUrl)
		dbBuilder.password(container.password)
		dbBuilder.username(container.username)
		dbBuilder.driverClassName(container.driverClassName)
		jdbcTemplate = JdbcTemplate(dbBuilder.build())
		jdbcTemplate.update(
			"""
					CREATE TABLE IF NOT EXISTS events(
                                     id SERIAL PRIMARY KEY NOT NULL ,
                                     topic VARCHAR(100) NULL,
                                     action_id VARCHAR(50) NULL,
                                     parent_action_id VARCHAR(50) NULL,
                                     action_type VARCHAR(255) NULL,
                                     message_originator VARCHAR(255) NULL,
                                     data TEXT NOT NULL,
                                     headers TEXT NOT NULL);
					CREATE INDEX ACTION_IDX ON events (topic, action_type);
		"""
		)
		setupConsumerLoop()
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> findEvents(kafkaEvent: KafkaEvent): List<Pair<T, KafkaHeaders>> {
		val topic = kafkaEvent.topicName
		val action_type = kafkaEvent.eventType
		val message_originator = kafkaEvent.messageOriginator
		val result = jdbcTemplate.queryForList(
			"SELECT data, headers FROM events WHERE topic=? AND action_type=? AND message_originator=?",
			topic,
			action_type,
			message_originator
		)
		return result.map {
			JsonUtil.getFromJson(it.getValue("DATA").toString(), kafkaEvent.kClass, true) to
					JsonUtil.getFromJson(it.getValue("HEADERS").toString(), KafkaHeaders::class, true)
		}.toList() as List<Pair<T, KafkaHeaders>>
	}

	@Suppress("UNCHECKED_CAST")
	fun <T : Any> findEvents(kafkaEvent: KafkaEvent, parentActionId: String): List<Pair<T, KafkaHeaders>> {
		val result = jdbcTemplate.queryForList(
			"SELECT data, headers FROM events WHERE topic=? AND action_type=? AND action_id=? AND message_originator=?",
			kafkaEvent.topicName, kafkaEvent.eventType, parentActionId, kafkaEvent.messageOriginator
		)

		return result.map {
			JsonUtil.getFromJson(it.getValue("DATA").toString(), kafkaEvent.kClass, true) to
					JsonUtil.getFromJson(it.getValue("HEADERS").toString(), KafkaHeaders::class, true)
		}.toList() as List<Pair<T, KafkaHeaders>>
	}

	private fun saveEvent(record: ConsumerRecord<String, String>) {
		try {
			val headers = record.headers().associate { it.key() to String(it.value()) }
			if (supportedEvents.contains(headers["actionType"])) {
				val actionId = record.key()
				jdbcTemplate.update(
					"INSERT INTO events(topic, action_id, parent_action_id, action_type, message_originator, data, headers) VALUES (?, ?, ?, ?, ?, ?, ?)",
					record.topic(),
					actionId,
					headers["parentActionId"],
					headers["actionType"],
					headers["messageOriginator"],
					record.value(),
					JsonUtil.getAsString(headers)
				)
			}
		} catch (t: Throwable) {
			t.printStackTrace()
		}
	}


	private fun setupConsumerLoop() {
		val consumer = KafkaConsumer<String, String>(getProperties())
		consumer.subscribe(topics)
		Thread {
			while (true) {
				try {
					consumer.poll(Duration.ofSeconds(2)).forEach { saveEvent(it) }
				} catch (t: Throwable) {
					t.printStackTrace()
				}

			}
		}.start()
	}

	private fun getProperties(): Properties {
		return Properties().apply {
			this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = KafkaContainer.getBootstrap()
			this[ConsumerConfig.GROUP_ID_CONFIG] = "consumerLoop-itTest"
			this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
			this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
			this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
			this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = "100"
			this[ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG] = ConsumerConfig.DEFAULT_FETCH_MAX_BYTES.times(5)
		}
	}
}



