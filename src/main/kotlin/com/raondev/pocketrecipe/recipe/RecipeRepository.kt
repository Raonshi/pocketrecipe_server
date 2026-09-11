package com.raondev.pocketrecipe.recipe

import java.util.UUID

interface RecipeRepository {
    fun create(recipe: Recipe, ownerId: UUID)
    fun findByNamePrefix(keyword: String): List<Recipe>
    fun update(recipe: Recipe, ownerId: UUID): Boolean
    fun deleteAll(recipes: List<RecipeIdentity>, ownerId: UUID): Int
}
