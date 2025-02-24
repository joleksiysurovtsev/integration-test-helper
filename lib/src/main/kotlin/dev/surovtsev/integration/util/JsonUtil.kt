package dev.surovtsev.integration.util

import org.springframework.data.domain.Page
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.io.IOException
import kotlin.jvm.java
import kotlin.reflect.KClass


object JsonUtil {

	private val objectMapper = createObjectMapper()

	private fun createObjectMapper(): ObjectMapper {
		return jacksonObjectMapper()
			.registerModule(JavaTimeModule())
			.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
			.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
	}

	fun <T : Any> getFromJson(body: String, toValueType: KClass<T>, ignoreUnknownProperties: Boolean): T {
		if (ignoreUnknownProperties) {
			val objectMapper = createObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			return objectMapper.readValue(body, toValueType.java)
		}
		return getFromJson(body, toValueType)
	}

	private fun <T : Any> getFromJson(body: String, toValueType: KClass<T>): T = objectMapper.readValue(body, toValueType.java)

	fun <T : Any> getPageFromJson(body: String, elementType: KClass<T>, ignoreUnknownProperties: Boolean): Page<T> {
		if (ignoreUnknownProperties) {
			val objectMapper = createObjectMapper()
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
			return objectMapper.readValue(body, objectMapper.typeFactory.constructParametricType(Page::class.java, elementType.java))
		}
		return getPageFromJson(body, elementType)
	}

	private fun <T : Any> getPageFromJson(body: String, elementType: KClass<T>): Page<T> {
		return objectMapper.readValue(body, objectMapper.typeFactory.constructParametricType(Page::class.java, elementType.java))
	}

	fun <T> getAsString(model: T): String = objectMapper.writeValueAsString(model)

	fun <T> deepCopy(model: T): T {
		return getFromJson(getAsString(model), model!!::class)
	}

	fun <V : Any, T: Any> convert(value: V, toType: KClass<T>): T {
		val asString = getAsString(value)
		return getFromJson(asString, toType, true)
	}

	fun <T> readList(str: String?, type: Class<out MutableCollection<*>?>?, elementType: Class<T>?): List<T>? {
		return try {
			val json = JSONParser().parse(str) as JSONObject
			val content = json["content"].toString()
			objectMapper.readValue(content, objectMapper.typeFactory.constructCollectionType(type, elementType))
		} catch (e: IOException) {
			throw RuntimeException(e)
		}
	}

	fun <T : Any> getListFromJson(body: String, elementType: KClass<T>, ignoreUnknownProperties: Boolean = false): List<T> {
		val objectMapper = if (ignoreUnknownProperties) {
			createObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		} else {
			this.objectMapper
		}
		return objectMapper.readValue(body, objectMapper.typeFactory.constructCollectionType(List::class.java, elementType.java))
	}

	fun <T : Any, V: Any> getMapFromJson(body: String, keyType: KClass<T>,
								 valueType: KClass<V>, ignoreUnknownProperties: Boolean = false): Map<T,V> {
		val objectMapper = if (ignoreUnknownProperties) {
			createObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		} else {
			this.objectMapper
		}
		return objectMapper.readValue(body,
			objectMapper.typeFactory.constructMapType(MutableMap::class.java, keyType.java,  valueType.java))
	}
}
