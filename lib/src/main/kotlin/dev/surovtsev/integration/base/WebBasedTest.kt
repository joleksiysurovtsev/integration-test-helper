package dev.surovtsev.integration.base

import dev.surovtsev.integration.util.JsonUtil
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.reflect.KClass

interface WebBasedTest{

    val mockMvc: MockMvc

    /**
     * Performs an HTTP GET request to "$baseUrl/{id}".
     *
     * This method sends a GET request, verifies the expected HTTP status, and then deserializes the response body
     * into an object of type [In]. If [inClass] is [String::class], the raw response string is returned.
     * If the response body is empty, it attempts to generate a JSON string from the response data.
     *
     * @param id The identifier of the resource to retrieve.
     * @param expectedStatusCode The expected HTTP status code.
     * @param inClass The class used for deserializing the response (e.g. AccountDto::class).
     * @param mapper A function that converts the deserialized object of type [In] into the final type [Out].
     *               By default, it simply casts the object.
     * @return An object of type [Out] obtained after deserialization and transformation using the mapper.
     */
    fun <In : Any, Out> get(
        id: Any,
        expectedStatusCode: ResultMatcher? = MockMvcResultMatchers.status().isOk,
        inClass: KClass<In>,
        mapper: (In) -> Out = { it as Out },
        url: String
    ): Out {
        val response = mockMvc.perform(MockMvcRequestBuilders.get(url))
            .andExpect(expectedStatusCode!!)
            .andReturn().response

        val content = response.contentAsString

        val inObject: In = if (inClass == String::class) {
            content as In
        } else if (content.isNotBlank()) {
            JsonUtil.getFromJson(content, inClass, true)
        } else {
            JsonUtil.getFromJson(JsonUtil.getAsString(response), inClass, true)
        }
        return mapper(inObject)
    }


    /**
     * Performs an HTTP POST request to [baseUrl] with the given request object [dto].
     *
     * This method sends a POST request with the serialized [dto] in the request body, verifies the expected HTTP status,
     * and then deserializes the response body into an object of type [In]. If [inClass] is [String::class], the raw response string is returned.
     * If the response body is empty, it uses a JSON string generated from the response data.
     *
     * @param dto The request object (e.g. AccountCreateDto).
     * @param expectedStatusCode The expected HTTP status code.
     * @param inClass The class used for deserializing the response (e.g. AccountDto::class).
     * @param mapper A function that converts the deserialized object of type [In] into the final type [Out].
     *               By default, it simply casts the object.
     * @return An object of type [Out] obtained after deserialization and transformation using the mapper.
     */
    fun <Req : Any, In : Any, Out> post(
        dto: Req,
        expectedStatusCode: ResultMatcher,
        inClass: KClass<In>,
        mapper: (In) -> Out = { it as Out },
        url: String? = null
    ): Out {
        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("$url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.getAsString(dto))
        )
            .andExpect(expectedStatusCode)
            .andReturn().response

        val content = response.contentAsString

        val inObject: In = if (inClass == String::class) {
            content as In
        } else if (content.isNotBlank()) {
            JsonUtil.getFromJson(content, inClass, true)
        } else {
            JsonUtil.getFromJson(JsonUtil.getAsString(response), inClass, true)
        }
        return mapper(inObject)
    }


    /**
     * Performs an HTTP DELETE request to "$baseUrl/{id}".
     *
     * This method sends a DELETE request, verifies the expected HTTP status,
     * and then deserializes the response body into an object of type [In]. If [inClass] is [String::class], the raw response string is returned.
     * If the response body is empty, it uses a JSON string generated from the response data.
     *
     * @param id The identifier of the resource to delete.
     * @param expectedStatusCode The expected HTTP status code.
     * @param inClass The class used for deserializing the response (e.g. ErrorResponse::class or String::class).
     * @param mapper A function that converts the deserialized object of type [In] into the final type [Out].
     *               By default, it simply casts the object.
     * @return An object of type [Out] obtained after deserialization and transformation using the mapper.
     */
    fun <In : Any, Out> delete(
        id: Any,
        expectedStatusCode: ResultMatcher,
        inClass: KClass<In>,
        mapper: (In) -> Out = { it as Out },
        url: String? = null
    ): Out {
        val response = mockMvc.perform(MockMvcRequestBuilders.delete("$url/$id"))
            .andExpect(expectedStatusCode)
            .andReturn().response

        val content = response.contentAsString

        val inObject: In = if (inClass == String::class) {
            content as In
        } else if (content.isNotBlank()) {
            JsonUtil.getFromJson(content, inClass, true)
        } else {
            JsonUtil.getFromJson(JsonUtil.getAsString(response), inClass, true)
        }
        return mapper(inObject)
    }

    /**
     * Main PUT method that accepts an optional request body.
     *
     * @param dto the request object to send (can be null if no body is required)
     * @param expectedStatusCode the expected HTTP status code (default is 200 OK)
     * @param inClass the KClass for deserializing the response (e.g. SystemProviderOutputDto::class)
     * @param mapper a function that converts the deserialized object of type [In] into the final type [Out]. By default, it casts the object.
     * @param url the URL to use; if null, uses the baseUrl.
     * @return an object of type [Out] obtained after deserialization and transformation using the mapper.
     */
    fun <Req : Any, In : Any, Out> put(
        dto: Req? = null,
        expectedStatusCode: ResultMatcher = MockMvcResultMatchers.status().isOk,
        inClass: KClass<In>,
        mapper: (In) -> Out = { it as Out },
        url: String
    ): Out {
        val requestBuilder = MockMvcRequestBuilders.put(url)
            .contentType(MediaType.APPLICATION_JSON)
        // Only add content if dto is provided
        if (dto != null) {
            requestBuilder.content(JsonUtil.getAsString(dto))
        }

        val response = mockMvc.perform(requestBuilder)
            .andExpect(expectedStatusCode)
            .andReturn().response

        val content = response.contentAsString

        val inObject: In = if (inClass == String::class) {
            content as In
        } else if (content.isNotBlank()) {
            JsonUtil.getFromJson(content, inClass, true)
        } else {
            JsonUtil.getFromJson(JsonUtil.getAsString(response), inClass, true)
        }
        return mapper(inObject)
    }

    /**
     * Overloaded PUT method for endpoints that do not require a request body.
     *
     * @param expectedStatusCode the expected HTTP status code (default is 200 OK)
     * @param inClass the KClass for deserializing the response (e.g. SystemProviderOutputDto::class)
     * @param mapper a function that converts the deserialized object of type [In] into the final type [Out]. By default, it casts the object.
     * @param url the URL to use; if null, uses the baseUrl.
     * @return an object of type [Out] obtained after deserialization and transformation using the mapper.
     */
    fun <In : Any, Out> put(
        expectedStatusCode: ResultMatcher = MockMvcResultMatchers.status().isOk,
        inClass: KClass<In>,
        mapper: (In) -> Out = { it as Out },
        url: String
    ): Out {
        return put<Unit, In, Out>(
            dto = null,
            expectedStatusCode = expectedStatusCode,
            inClass = inClass,
            mapper = mapper,
            url = url
        )
    }
}