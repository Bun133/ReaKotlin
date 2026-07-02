package server.http

import server.header.RequestHeader

data class Request(
    val header: RequestHeader,
    val body: ByteArray
)