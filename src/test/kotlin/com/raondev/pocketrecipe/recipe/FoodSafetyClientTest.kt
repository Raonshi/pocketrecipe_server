package com.raondev.pocketrecipe.recipe

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.client.RestClient
import com.raondev.pocketrecipe.security.SecurityProperties

class FoodSafetyClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: FoodSafetyClient

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = FoodSafetyClient(
            FoodSafetyProperties(baseUrl = server.url("/api").toString().removeSuffix("/"), apiKey = "test-key"),
            jacksonObjectMapper(),
            SecurityProperties(enforcement = false),
            RestClient.builder(),
        )
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `returns rows from Food Safety API`() {
        server.enqueue(
            MockResponse().setBody("""{"COOKRCP01":{"row":[{"RCP_NM":"Pasta"}]}}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = client.search("pasta sauce")

        assertEquals("Pasta", result[0]["RCP_NM"].asText())
        assertEquals("/api/test-key/COOKRCP01/json/1/5?RCP_NM=pasta%20sauce", server.takeRequest().path)
    }

    @Test
    fun `uses legacy empty marker when API has no rows`() {
        server.enqueue(
            MockResponse().setBody("""{"COOKRCP01":{"total_count":"0"}}""")
                .addHeader("Content-Type", "application/json"),
        )

        val result = client.search("missing")

        assertEquals("", result[0].asText())
    }

    @Test
    fun `rejects an unset API key`() {
        val missingKeyClient = FoodSafetyClient(
            FoodSafetyProperties(baseUrl = server.url("/api").toString(), apiKey = ""),
            jacksonObjectMapper(),
            SecurityProperties(enforcement = false),
            RestClient.builder(),
        )

        assertThrows<IllegalArgumentException> { missingKeyClient.search("pasta") }
    }
}
