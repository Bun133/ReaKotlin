package server.http

import server.header.HTTPVersion
import server.header.ResponseHeader

private val ResponseHTTPVersion = HTTPVersion(1, 1)

class Response(
    val header: ResponseHeader,
    val body: ByteArray,
) {
    companion object {
        fun string(body: String): Response {
            return Response(
                header = ResponseHeader(
                    httpVersion = ResponseHTTPVersion, keyValue = mapOf()
                ),
                body = body.toByteArray(),
            )
        }
    }
}