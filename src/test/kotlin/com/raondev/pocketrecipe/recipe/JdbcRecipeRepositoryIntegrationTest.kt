package com.raondev.pocketrecipe.recipe

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import java.util.UUID

@EnabledIfEnvironmentVariable(named = "INTEGRATION_DB_URL", matches = ".+")
class JdbcRecipeRepositoryIntegrationTest {
    private lateinit var repository: JdbcRecipeRepository

    @BeforeEach
    fun setUp() {
        val dataSource = DriverManagerDataSource(
            System.getenv("INTEGRATION_DB_URL"),
            System.getenv("INTEGRATION_DB_USERNAME"),
            System.getenv("INTEGRATION_DB_PASSWORD"),
        )
        dataSource.connection.use { connection -> connection.createStatement().use { it.executeUpdate("DELETE FROM recipes") } }
        repository = JdbcRecipeRepository(NamedParameterJdbcTemplate(dataSource))
    }

    @Test
    fun `creates searches updates and deletes a recipe`() {
        val created = Recipe(
            name = "Kimchi stew",
            author = "raon",
            ownerId = null,
            parts = "kimchi, pork",
            energy = 200,
            sodium = 500,
            carbohydrate = 10,
            protein = 15,
            fat = 8,
            manuals = listOf("Boil stock", "Add kimchi"),
        )
        val ownerId = UUID.randomUUID()
        repository.create(created, ownerId)

        assertEquals(listOf(created.copy(ownerId = ownerId)), repository.findByNamePrefix("Kimchi"))

        val updated = created.copy(manuals = listOf("Simmer for 20 minutes"))
        assertEquals(true, repository.update(updated, ownerId))
        assertEquals(listOf(updated.copy(ownerId = ownerId)), repository.findByNamePrefix("Kimchi"))

        assertEquals(1, repository.deleteAll(listOf(RecipeIdentity("Kimchi stew", "raon")), ownerId))
        assertEquals(emptyList<Recipe>(), repository.findByNamePrefix("Kimchi"))
    }
}
