package com.raondev.pocketrecipe

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PocketRecipeApplication

fun main(args: Array<String>) {
    runApplication<PocketRecipeApplication>(*args)
}
