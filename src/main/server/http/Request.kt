package server.http

import server.header.RequestHeader

class Request(
    val header: RequestHeader,
    val body: ByteArray
) {
}