package com.raondev.pocketrecipe.security

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.OAuth2TokenValidator
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtValidators
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties::class)
class SecurityConfiguration(
    private val properties: SecurityProperties,
    private val rateLimitFilter: RateLimitFilter,
    private val apiSecurityHandlers: ApiSecurityHandlers,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers {
                it.contentSecurityPolicy { policy -> policy.policyDirectives("default-src 'none'; base-uri 'none'; frame-ancestors 'none'") }
                it.frameOptions { frame -> frame.deny() }
                it.referrerPolicy { policy -> policy.policy(ReferrerPolicy.NO_REFERRER) }
                it.cacheControl(Customizer.withDefaults())
                it.httpStrictTransportSecurity { hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31_536_000) }
            }
            .exceptionHandling {
                it.authenticationEntryPoint(apiSecurityHandlers)
                it.accessDeniedHandler(apiSecurityHandlers)
            }

        if (properties.enforcement) {
            http
                .authorizeHttpRequests {
                    it.requestMatchers(HttpMethod.GET, "/search-recipe").permitAll()
                        .anyRequest().authenticated()
                }
                .oauth2ResourceServer { it.jwt(Customizer.withDefaults()) }
                .addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter::class.java)
        } else {
            http.authorizeHttpRequests { it.anyRequest().permitAll() }
                .addFilterAfter(rateLimitFilter, BearerTokenAuthenticationFilter::class.java)
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOrigins = properties.allowedOrigins.filter { it.isNotBlank() }
            allowedMethods = listOf("GET", "PUT", "POST", "DELETE", "OPTIONS")
            allowedHeaders = listOf("Authorization", "Content-Type")
            allowCredentials = false
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().apply { registerCorsConfiguration("/**", configuration) }
    }

    @Bean
    @ConditionalOnProperty(name = ["app.security.enforcement"], havingValue = "true", matchIfMissing = true)
    fun jwtDecoder(): JwtDecoder {
        val decoder = NimbusJwtDecoder.withJwkSetUri(properties.jwtJwkSetUri)
            .jwsAlgorithms { algorithms ->
                algorithms.add(SignatureAlgorithm.RS256)
                algorithms.add(SignatureAlgorithm.ES256)
            }
            .build()
        decoder.setJwtValidator(
            DelegatingOAuth2TokenValidator(
                JwtValidators.createDefaultWithIssuer(properties.jwtIssuerUri),
                AudienceValidator(properties.jwtAudience),
            ),
        )
        return decoder
    }
}

private class AudienceValidator(private val expectedAudience: String) : OAuth2TokenValidator<Jwt> {
    override fun validate(token: Jwt): OAuth2TokenValidatorResult =
        if (token.audience.contains(expectedAudience)) OAuth2TokenValidatorResult.success()
        else OAuth2TokenValidatorResult.failure(OAuth2Error("invalid_token", "Invalid audience", null))
}
