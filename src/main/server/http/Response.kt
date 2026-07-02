package server.http

import server.ContentType
import server.MIME
import server.header.HTTPVersion
import server.header.ResponseHeader

private val ResponseHTTPVersion = HTTPVersion(1, 1)

data class Response(
    val header: ResponseHeader,
    val body: ByteArray,
) {
    companion object {
        fun string(body: String): Response {
            return Response(
                header = ResponseHeader(
                    httpVersion = ResponseHTTPVersion, keyValue = mapOf(),
                    contentType = ContentType(MIME.TEXT)
                ),
                body = body.toByteArray(),
            )
        }
    }
}