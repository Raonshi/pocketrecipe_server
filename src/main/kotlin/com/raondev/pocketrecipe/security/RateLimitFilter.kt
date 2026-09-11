package com.raondev.pocketrecipe.security

import com.github.benmanes.caffeine.cache.Caffeine
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

@Component
class RateLimitFilter : OncePerRequestFilter() {
    private val counters = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(2))
        .maximumSize(50_000)
        .build<String, FixedWindowCounter>()

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI !in setOf("/search-recipe", "/insert-recipe", "/update-recipe", "/delete-recipe")

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val isSearch = request.method == "GET" && request.requestURI == "/search-recipe"
        val principal = (SecurityContextHolder.getContext().authentication?.principal as? Jwt)?.subject
        val key = if (isSearch) "search:${request.remoteAddr}" else "write:${principal ?: request.remoteAddr}"
        val limit = if (isSearch) 60 else 20
        if (!counters.get(key) { FixedWindowCounter() }.tryConsume(limit)) {
            response.status = 429
            response.contentType = "application/json"
            response.setHeader("Retry-After", "60")
            response.writer.write("{\"message\":\"Too many requests\"}")
            return
        }
        filterChain.doFilter(request, response)
    }
}

private class FixedWindowCounter {
    private var windowStartedAt = System.currentTimeMillis()
    private val count = AtomicInteger(0)

    @Synchronized
    fun tryConsume(limit: Int): Boolean {
        if (System.currentTimeMillis() - windowStartedAt >= 60_000) {
            windowStartedAt = System.currentTimeMillis()
            count.set(0)
        }
        return count.incrementAndGet() <= limit
    }
}
