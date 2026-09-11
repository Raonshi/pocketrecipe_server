package com.raondev.pocketrecipe.recipe

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.util.UriComponentsBuilder
import com.raondev.pocketrecipe.security.SecurityProperties
import java.net.URI
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

@ConfigurationProperties("app.food-safety")
data class FoodSafetyProperties(
    var baseUrl: String = "https://openapi.foodsafetykorea.go.kr/api",
    var apiKey: String = "",
    var mockEnabled: Boolean = false,
)

@Component
@EnableConfigurationProperties(FoodSafetyProperties::class)
class FoodSafetyClient(
    private val properties: FoodSafetyProperties,
    private val objectMapper: ObjectMapper,
    securityProperties: SecurityProperties,
    restClientBuilder: RestClient.Builder,
) {
    private val restClient = restClientBuilder.requestFactory(SimpleClientHttpRequestFactory().apply {
        setConnectTimeout(2_000)
        setReadTimeout(5_000)
    }).build()

    init {
        if (securityProperties.enforcement) {
            val configured = URI.create(properties.baseUrl)
            require(configured.scheme == "https" && configured.host == "openapi.foodsafetykorea.go.kr") {
                "Food Safety API URL must use the approved HTTPS host"
            }
            require(properties.apiKey.isNotBlank() && !properties.mockEnabled) {
                "FOOD_SAFETY_API_KEY must be configured when security enforcement is enabled"
            }
        }
    }

    fun search(keyword: String): JsonNode {
        if (properties.mockEnabled) {
            return objectMapper.createArrayNode().add("")
        }
        require(properties.apiKey.isNotBlank()) { "FOOD_SAFETY_API_KEY must be configured" }

        val uri = UriComponentsBuilder.fromUriString(properties.baseUrl)
            .pathSegment(properties.apiKey, "COOKRCP01", "json", "1", "5")
            .queryParam("RCP_NM", keyword)
            .build()
            .encode()
            .toUri()

        val response = try {
            restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .exchange { _, clientResponse ->
                    if (!clientResponse.statusCode.is2xxSuccessful) {
                        throw FoodSafetyApiException("Food Safety API request failed")
                    }
                    if (clientResponse.headers.contentLength > MAX_RESPONSE_BYTES) {
                        throw FoodSafetyApiException("Food Safety API response is too large")
                    }
                    objectMapper.readTree(BoundedInputStream(clientResponse.body, MAX_RESPONSE_BYTES))
                }
                ?: throw FoodSafetyApiException("Food Safety API returned an empty response")
        } catch (exception: RestClientException) {
            throw FoodSafetyApiException("Food Safety API request failed", exception)
        } catch (exception: IOException) {
            throw FoodSafetyApiException("Food Safety API response could not be read", exception)
        }

        return response.path("COOKRCP01").path("row").takeIf { it.isArray }
            ?: objectMapper.createArrayNode().add("")
    }
}

private class BoundedInputStream(input: InputStream, private val maximumBytes: Long) : FilterInputStream(input) {
    private var bytesRead = 0L

    override fun read(): Int = super.read().also { if (it != -1) count(1) }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int = super.read(buffer, offset, length).also {
        if (it > 0) count(it.toLong())
    }

    private fun count(read: Long) {
        bytesRead += read
        if (bytesRead > maximumBytes) throw IOException("Response exceeds $maximumBytes bytes")
    }
}

private const val MAX_RESPONSE_BYTES = 1_048_576L

class FoodSafetyApiException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
