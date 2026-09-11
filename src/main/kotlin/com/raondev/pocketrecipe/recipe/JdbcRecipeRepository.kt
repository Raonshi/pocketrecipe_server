package com.raondev.pocketrecipe.recipe

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class JdbcRecipeRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
) : RecipeRepository {
    override fun create(recipe: Recipe, ownerId: UUID) {
        jdbcTemplate.update(
            """
            INSERT INTO recipes (
                recipe_name, recipe_author, recipe_parts, recipe_energy, recipe_nat,
                recipe_cal, recipe_pro, recipe_fat, recipe_manual, owner_id
            ) VALUES (
                :name, :author, :parts, :energy, :sodium, :carbohydrate, :protein, :fat, :manuals, :ownerId
            )
            """.trimIndent(),
            parameters(recipe).addValue("ownerId", ownerId),
        )
    }

    override fun findByNamePrefix(keyword: String): List<Recipe> =
        jdbcTemplate.query(
            """
            SELECT recipe_name, recipe_author, recipe_parts, recipe_energy, recipe_nat,
                   recipe_cal, recipe_pro, recipe_fat, recipe_manual, owner_id
            FROM recipes
            WHERE recipe_name ILIKE :keyword ESCAPE '\\'
            ORDER BY recipe_name, recipe_author
            LIMIT 20
            """.trimIndent(),
            mapOf("keyword" to "${escapeLike(keyword)}%"),
        ) { resultSet, _ ->
            val manualArray = resultSet.getArray("recipe_manual")?.array as? Array<*> ?: emptyArray<String>()
            Recipe(
                name = resultSet.getString("recipe_name"),
                author = resultSet.getString("recipe_author"),
                ownerId = resultSet.getObject("owner_id", UUID::class.java),
                parts = resultSet.getString("recipe_parts"),
                energy = resultSet.getObject("recipe_energy", Int::class.javaObjectType),
                sodium = resultSet.getObject("recipe_nat", Int::class.javaObjectType),
                carbohydrate = resultSet.getObject("recipe_cal", Int::class.javaObjectType),
                protein = resultSet.getObject("recipe_pro", Int::class.javaObjectType),
                fat = resultSet.getObject("recipe_fat", Int::class.javaObjectType),
                manuals = manualArray.map { it.toString() },
            )
        }

    override fun update(recipe: Recipe, ownerId: UUID): Boolean =
        jdbcTemplate.update(
            """
            UPDATE recipes
            SET recipe_parts = :parts,
                recipe_energy = :energy,
                recipe_nat = :sodium,
                recipe_cal = :carbohydrate,
                recipe_pro = :protein,
                recipe_fat = :fat,
                recipe_manual = :manuals
            WHERE recipe_name = :name AND recipe_author = :author AND owner_id = :ownerId
            """.trimIndent(),
            parameters(recipe).addValue("ownerId", ownerId),
        ) == 1

    override fun deleteAll(recipes: List<RecipeIdentity>, ownerId: UUID): Int =
        recipes.sumOf { recipe ->
            jdbcTemplate.update(
                "DELETE FROM recipes WHERE recipe_name = :name AND recipe_author = :author AND owner_id = :ownerId",
                mapOf("name" to recipe.recipeName, "author" to recipe.normalizedAuthor(), "ownerId" to ownerId),
            )
        }

    private fun parameters(recipe: Recipe) = MapSqlParameterSource()
        .addValue("name", recipe.name)
        .addValue("author", recipe.author)
        .addValue("parts", recipe.parts)
        .addValue("energy", recipe.energy)
        .addValue("sodium", recipe.sodium)
        .addValue("carbohydrate", recipe.carbohydrate)
        .addValue("protein", recipe.protein)
        .addValue("fat", recipe.fat)
        .addValue("manuals", recipe.manuals.toTypedArray())

    private fun escapeLike(keyword: String): String = keyword
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
}
