package com.raondev.pocketrecipe.recipe

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.test.context.TestPropertySource
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(RecipeController::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = ["app.security.enforcement=false", "app.security.allowed-origins=http://localhost:3000"])
class RecipeControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean
    private lateinit var recipeService: RecipeService

    @MockitoBean
    private lateinit var ownerIdResolver: com.raondev.pocketrecipe.security.OwnerIdResolver

    @Test
    fun `search keeps the legacy response keys`() {
        val openRecipes = JsonNodeFactory.instance.arrayNode().apply {
            addObject().put("RCP_NM", "Open pasta")
        }
        `when`(recipeService.search("pasta")).thenReturn(
            RecipeSearchResponse(
                openRecipes = openRecipes,
                storedRecipes = listOf(
                    StoredRecipeResponse("My pasta", null, 100, null, null, null, null, "guest", emptyList()),
                ),
            ),
        )

        mockMvc.perform(get("/search-recipe").queryParam("keyword", "pasta"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.op_recipe[0].RCP_NM").value("Open pasta"))
            .andExpect(jsonPath("$.fs_recipe[0].RCP_NM").value("My pasta"))
    }

    @Test
    fun `insert validates and returns the legacy mutation response`() {
        `when`(ownerIdResolver.resolve(null)).thenReturn(UUID(0, 0))
        mockMvc.perform(
            put("/insert-recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"recipe_name":"Kimchi stew","recipe_author":"raon"}"""),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.Response").value("OK"))
    }

    @Test
    fun `delete rejects a count that does not match its recipe list`() {
        val request = mapOf(
            "recipeList" to listOf(mapOf("recipe_name" to "Kimchi stew", "recipe_author" to "raon")),
            "count" to 2,
        )

        mockMvc.perform(
            delete("/delete-recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isBadRequest)
    }
}
