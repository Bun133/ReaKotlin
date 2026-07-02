package server.header

import server.ContentType

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
    override val httpVersion: HTTPVersion,
    override val keyValue: Map<String, String>,
    val contentType: ContentType
) : Header {
    fun toHeaderLines(): List<String>{
        val contentTypeLine = "Content-Type: ${contentType.mime.value}"
        val headerLines = keyValue.map { "${it.key}: ${it.value}" }

        val lines = mutableListOf<String>()
        lines.add(contentTypeLine)
        lines.addAll(headerLines)

        return lines
    }
}

data class RequestHeader(
    override val httpVersion: HTTPVersion,
    val target: String,
    val method: Method,
    override val keyValue: Map<String, String>,
) : Header