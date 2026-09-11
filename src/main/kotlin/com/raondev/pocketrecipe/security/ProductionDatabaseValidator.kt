package com.raondev.pocketrecipe.security

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class ProductionDatabaseValidator(
    private val securityProperties: SecurityProperties,
    @Value("\${spring.datasource.url}") private val databaseUrl: String,
) {
    @PostConstruct
    fun validate() {
        if (!securityProperties.enforcement) return
        require(databaseUrl.startsWith("jdbc:postgresql://") && databaseUrl.contains("sslmode=verify-full")) {
            "Production PostgreSQL must use jdbc:postgresql with sslmode=verify-full"
        }
    }
}
