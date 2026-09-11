package com.raondev.pocketrecipe.security

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@Validated
@ConfigurationProperties("app.security")
data class SecurityProperties(
    var enforcement: Boolean = true,
    var allowedOrigins: List<String> = emptyList(),
    var jwtIssuerUri: String = "",
    var jwtJwkSetUri: String = "",
    var jwtAudience: String = "authenticated",
) {
    @PostConstruct
    fun validateProductionSettings() {
        if (!enforcement) return
        require(allowedOrigins.isNotEmpty() && allowedOrigins.none { it.isBlank() }) {
            "APP_SECURITY_ALLOWED_ORIGINS must be configured when security enforcement is enabled"
        }
        require(jwtIssuerUri.startsWith("https://")) {
            "APP_SECURITY_JWT_ISSUER_URI must be an HTTPS URL"
        }
        require(jwtJwkSetUri.startsWith("https://")) {
            "APP_SECURITY_JWT_JWK_SET_URI must be an HTTPS URL"
        }
    }
}
