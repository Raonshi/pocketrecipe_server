package com.raondev.pocketrecipe.security

import com.raondev.pocketrecipe.recipe.InvalidOwnerException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OwnerIdResolver(
    private val properties: SecurityProperties,
) {
    fun resolve(jwt: Jwt?): UUID {
        if (jwt == null) {
            if (!properties.enforcement) return LOCAL_OWNER_ID
            throw InvalidOwnerException()
        }
        return try {
            UUID.fromString(jwt.subject)
        } catch (exception: IllegalArgumentException) {
            throw InvalidOwnerException()
        }
    }

    private companion object {
        val LOCAL_OWNER_ID: UUID = UUID(0, 0)
    }
}
