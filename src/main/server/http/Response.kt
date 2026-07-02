package server.http

import server.header.HTTPVersion
import server.header.ResponseHeader

private val ResponseHTTPVersion = HTTPVersion(1, 1)

class Response(
    val header: ResponseHeader,
    val body: ByteArray,
) {
    companion object {
        fun string(body: String, code: HTTPCode = HTTPCode.OK): Response {
            return Response(
                header = ResponseHeader(
                    code, httpVersion = ResponseHTTPVersion, keyValue = mapOf()
                ),
                body = body.toByteArray(),
            )
        }
    }
}