package com.raondev.pocketrecipe.security

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.raondev.pocketrecipe.recipe.RecipeController
import com.raondev.pocketrecipe.recipe.RecipeSearchResponse
import com.raondev.pocketrecipe.recipe.RecipeService
import com.raondev.pocketrecipe.recipe.StoredRecipeResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RecipeController::class)
@Import(SecurityConfiguration::class, RateLimitFilter::class, ApiSecurityHandlers::class)
@TestPropertySource(
    properties = [
        "app.security.enforcement=true",
        "app.security.allowed-origins=https://app.example.com",
        "app.security.jwt-issuer-uri=https://project.supabase.co/auth/v1",
        "app.security.jwt-jwk-set-uri=https://project.supabase.co/auth/v1/.well-known/jwks.json",
    ],
)
class ApiSecurityConfigurationTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockitoBean
    private lateinit var recipeService: RecipeService

    @MockitoBean
    private lateinit var ownerIdResolver: OwnerIdResolver

    @MockitoBean(name = "jwtDecoder")
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `search remains public and sends security headers`() {
        `when`(recipeService.search("pasta")).thenReturn(
            RecipeSearchResponse(JsonNodeFactory.instance.arrayNode(), emptyList<StoredRecipeResponse>()),
        )

        mockMvc.perform(get("/search-recipe").queryParam("keyword", "pasta"))
            .andExpect(status().isOk)
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().string("Content-Security-Policy", "default-src 'none'; base-uri 'none'; frame-ancestors 'none'"))
    }

    @Test
    fun `mutation requires bearer authentication`() {
        mockMvc.perform(
            put("/insert-recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"recipe_name":"Kimchi stew"}"""),
        )
            .andExpect(status().isUnauthorized)
    }
}
