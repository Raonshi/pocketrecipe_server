package com.raondev.pocketrecipe.recipe

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.Valid
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import java.util.UUID

data class Recipe(
    val name: String,
    val author: String,
    val ownerId: UUID?,
    val parts: String?,
    val energy: Int?,
    val sodium: Int?,
    val carbohydrate: Int?,
    val protein: Int?,
    val fat: Int?,
    val manuals: List<String>,
)

data class RecipeRequest(
    @field:NotBlank
    @field:Size(max = 200)
    @JsonProperty("recipe_name")
    val recipeName: String,
    @JsonProperty("recipe_parts")
    @field:Size(max = 10_000)
    val recipeParts: String? = null,
    @JsonProperty("recipe_energy")
    @field:Min(0)
    @field:Max(1_000_000)
    val recipeEnergy: Int? = null,
    @JsonProperty("recipe_nat")
    @field:Min(0)
    @field:Max(1_000_000)
    val recipeNat: Int? = null,
    @JsonProperty("recipe_cal")
    @field:Min(0)
    @field:Max(1_000_000)
    val recipeCal: Int? = null,
    @JsonProperty("recipe_pro")
    @field:Min(0)
    @field:Max(1_000_000)
    val recipePro: Int? = null,
    @JsonProperty("recipe_fat")
    @field:Min(0)
    @field:Max(1_000_000)
    val recipeFat: Int? = null,
    @JsonProperty("recipe_author")
    @field:Size(max = 100)
    val recipeAuthor: String? = null,
    @JsonProperty("recipe_manual")
    @field:Size(max = 20)
    val recipeManual: List<@Size(max = 5_000) String> = emptyList(),
) {
    fun toRecipe() = Recipe(
        name = recipeName,
        author = recipeAuthor?.takeIf { it.isNotBlank() } ?: "guest",
        ownerId = null,
        parts = recipeParts,
        energy = recipeEnergy,
        sodium = recipeNat,
        carbohydrate = recipeCal,
        protein = recipePro,
        fat = recipeFat,
        manuals = recipeManual,
    )
}

data class RecipeIdentity(
    @field:NotBlank
    @field:Size(max = 200)
    @JsonProperty("recipe_name")
    val recipeName: String,
    @JsonProperty("recipe_author")
    @field:Size(max = 100)
    val recipeAuthor: String? = null,
) {
    fun normalizedAuthor() = recipeAuthor?.takeIf { it.isNotBlank() } ?: "guest"
}

data class DeleteRecipesRequest(
    @field:NotEmpty
    @field:Size(max = 20)
    @field:Valid
    @JsonProperty("recipeList")
    val recipeList: List<RecipeIdentity>,
    @field:Positive
    val count: Int,
) {
    @get:AssertTrue(message = "count must match the number of recipes in recipeList")
    @get:JsonIgnore
    val isCountMatching: Boolean
        get() = count == recipeList.size
}

data class StoredRecipeResponse(
    @JsonProperty("RCP_NM")
    val name: String,
    @JsonProperty("RCP_PARTS")
    val parts: String?,
    @JsonProperty("INFO_ENG")
    val energy: Int?,
    @JsonProperty("INFO_NA")
    val sodium: Int?,
    @JsonProperty("INFO_CAR")
    val carbohydrate: Int?,
    @JsonProperty("INFO_PRO")
    val protein: Int?,
    @JsonProperty("INFO_FAT")
    val fat: Int?,
    @JsonProperty("RCP_AUTHOR")
    val author: String,
    @JsonProperty("MANUALS")
    val manuals: List<String>,
) {
    companion object {
        fun from(recipe: Recipe) = StoredRecipeResponse(
            name = recipe.name,
            parts = recipe.parts,
            energy = recipe.energy,
            sodium = recipe.sodium,
            carbohydrate = recipe.carbohydrate,
            protein = recipe.protein,
            fat = recipe.fat,
            author = recipe.author,
            manuals = recipe.manuals,
        )
    }
}

data class MutationResponse(
    @JsonProperty("Response")
    val response: String = "OK",
)
