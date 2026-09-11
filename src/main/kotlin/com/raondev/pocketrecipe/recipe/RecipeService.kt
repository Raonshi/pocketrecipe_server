package com.raondev.pocketrecipe.recipe

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val foodSafetyClient: FoodSafetyClient,
) {
    fun search(keyword: String): RecipeSearchResponse = RecipeSearchResponse(
        openRecipes = foodSafetyClient.search(keyword),
        storedRecipes = recipeRepository.findByNamePrefix(keyword).map(StoredRecipeResponse::from),
    )

    fun create(request: RecipeRequest, ownerId: UUID) {
        try {
            recipeRepository.create(request.toRecipe(), ownerId)
        } catch (exception: DuplicateKeyException) {
            throw RecipeAlreadyExistsException(request.recipeName, request.recipeAuthor ?: "guest")
        }
    }

    fun update(request: RecipeRequest, ownerId: UUID) {
        val recipe = request.toRecipe()
        if (!recipeRepository.update(recipe, ownerId)) {
            throw RecipeNotFoundException()
        }
    }

    @Transactional
    fun delete(request: DeleteRecipesRequest, ownerId: UUID) {
        require(request.count == request.recipeList.size) {
            "count must match the number of recipes in recipeList"
        }
        val deleted = recipeRepository.deleteAll(request.recipeList, ownerId)
        if (deleted != request.count) {
            throw RecipeNotFoundException()
        }
    }
}

class RecipeNotFoundException(name: String? = null, author: String? = null) : RuntimeException(
    if (name == null) "One or more recipes were not found" else "Recipe '$name' by '$author' was not found",
)

class RecipeAlreadyExistsException(name: String, author: String) : RuntimeException(
    "Recipe '$name' by '$author' already exists",
)
