package server.header

import server.http.HTTPCode

enum class Method {
    GET, POST, PUT, PATCH, DELETE, CONNECT, OPTIONS, TRACE
}

data class HTTPVersion(
    val major: Int,
    val minor: Int,
) {
    override fun toString(): String {
        return "HTTP/$major.$minor"
    }
}

sealed interface Header {
    val httpVersion: HTTPVersion
    val keyValue: Map<String, String>
}

data class ResponseHeader(
    val code: HTTPCode,
    override val httpVersion: HTTPVersion,
    override val keyValue: Map<String, String>
) : Header

data class RequestHeader(
    override val httpVersion: HTTPVersion,
    val target: String,
    val method: Method,
    override val keyValue: Map<String, String>,
) : Header