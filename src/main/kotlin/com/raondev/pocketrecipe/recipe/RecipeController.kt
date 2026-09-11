package com.raondev.pocketrecipe.recipe

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.core.annotation.AuthenticationPrincipal
import com.raondev.pocketrecipe.security.OwnerIdResolver

data class RecipeSearchResponse(
    @JsonProperty("op_recipe")
    val openRecipes: JsonNode,
    @JsonProperty("fs_recipe")
    val storedRecipes: List<StoredRecipeResponse>,
)

@RestController
@Validated
@RequestMapping
class RecipeController(
    private val recipeService: RecipeService,
    private val ownerIdResolver: OwnerIdResolver,
) {
    @GetMapping("/search-recipe")
    fun searchRecipe(@RequestParam @NotBlank @Size(max = 100) keyword: String): RecipeSearchResponse = recipeService.search(keyword)

    @PutMapping("/insert-recipe")
    fun insertRecipe(@Valid @RequestBody recipe: RecipeRequest, @AuthenticationPrincipal jwt: Jwt?): MutationResponse {
        recipeService.create(recipe, ownerIdResolver.resolve(jwt))
        return MutationResponse()
    }

    @PostMapping("/update-recipe")
    fun updateRecipe(@Valid @RequestBody recipe: RecipeRequest, @AuthenticationPrincipal jwt: Jwt?): MutationResponse {
        recipeService.update(recipe, ownerIdResolver.resolve(jwt))
        return MutationResponse()
    }

    @DeleteMapping("/delete-recipe")
    fun deleteRecipe(@Valid @RequestBody request: DeleteRecipesRequest, @AuthenticationPrincipal jwt: Jwt?): MutationResponse {
        recipeService.delete(request, ownerIdResolver.resolve(jwt))
        return MutationResponse()
    }
}

class InvalidOwnerException : RuntimeException()
