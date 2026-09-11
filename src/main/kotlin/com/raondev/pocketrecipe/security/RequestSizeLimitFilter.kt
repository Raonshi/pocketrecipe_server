package com.raondev.pocketrecipe.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class RequestSizeLimitFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        if (request.contentLengthLong > MAX_BODY_BYTES) {
            writeTooLarge(response)
            return
        }
        try {
            filterChain.doFilter(LimitedRequest(request), response)
        } catch (exception: PayloadTooLargeException) {
            if (!response.isCommitted) writeTooLarge(response)
        }
    }

    private fun writeTooLarge(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE
        response.contentType = "application/json"
        response.writer.write("{\"message\":\"Request body is too large\"}")
    }

    private class LimitedRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
        override fun getInputStream(): ServletInputStream = LimitedServletInputStream(super.getInputStream(), MAX_BODY_BYTES)

        override fun getReader(): BufferedReader = BufferedReader(InputStreamReader(inputStream, characterEncoding ?: "UTF-8"))
    }

    private class LimitedServletInputStream(
        private val delegate: ServletInputStream,
        private val maxBytes: Long,
    ) : ServletInputStream() {
        private var bytesRead = 0L

        override fun read(): Int = delegate.read().also { if (it != -1) count(1) }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = delegate.read(buffer, offset, length).also {
            if (it > 0) count(it.toLong())
        }

        override fun isFinished(): Boolean = delegate.isFinished
        override fun isReady(): Boolean = delegate.isReady
        override fun setReadListener(readListener: ReadListener) = delegate.setReadListener(readListener)

        private fun count(read: Long) {
            bytesRead += read
            if (bytesRead > maxBytes) throw PayloadTooLargeException()
        }
    }

    private class PayloadTooLargeException : IOException()

    private companion object {
        const val MAX_BODY_BYTES = 128 * 1024L
    }
}
