package com.raondev.pocketrecipe.api

import com.raondev.pocketrecipe.recipe.FoodSafetyApiException
import com.raondev.pocketrecipe.recipe.RecipeAlreadyExistsException
import com.raondev.pocketrecipe.recipe.RecipeNotFoundException
import com.raondev.pocketrecipe.recipe.InvalidOwnerException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.validation.ConstraintViolationException

data class ApiError(val message: String)

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class, ConstraintViolationException::class, IllegalArgumentException::class)
    fun badRequest(exception: Exception): ResponseEntity<ApiError> =
        ResponseEntity.badRequest().body(ApiError(exception.message ?: "Invalid request"))

    @ExceptionHandler(RecipeNotFoundException::class)
    fun notFound(exception: RecipeNotFoundException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError(exception.message ?: "Recipe not found"))

    @ExceptionHandler(InvalidOwnerException::class)
    fun invalidOwner(exception: InvalidOwnerException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiError("Access is denied"))

    @ExceptionHandler(RecipeAlreadyExistsException::class)
    fun conflict(exception: RecipeAlreadyExistsException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError("Recipe already exists"))

    @ExceptionHandler(FoodSafetyApiException::class)
    fun upstreamFailure(exception: FoodSafetyApiException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(ApiError(exception.message ?: "Food Safety API failed"))

    @ExceptionHandler(DataAccessException::class)
    fun databaseFailure(exception: DataAccessException): ResponseEntity<ApiError> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiError("Database operation failed"))
}
