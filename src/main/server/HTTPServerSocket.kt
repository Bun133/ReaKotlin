package server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

private enum class State {
    HEADER,
    BODY,
}

enum class Method {
    GET, POST, PUT, PATCH, DELETE, CONNECT, OPTIONS, TRACE
}

data class HTTPVersion(
    val major: Int,
    val minor: Int,
)

sealed interface Header {
    val httpVersion: HTTPVersion
}

data class ResponseHeader(
    val status: Int, val headers: Map<String, String>, override val httpVersion: HTTPVersion
) : Header

data class RequestHeader(
    override val httpVersion: HTTPVersion,
    val target: String,
    val method: Method
) : Header

data class Message(
    val header: Header
)

open class HTTPServerSocket(port: Int) : BaseServerSocket<Message>(port) {
    private var status: State = State.HEADER
    private var header: Header? = null

    // on Message Received
    open fun onMessageReceived(message: Message) {}

    override suspend fun handleStream(scope: CoroutineScope, inputStream: InputStream): Message {
        return withContext(Dispatchers.IO) {
            val reader = inputStream.bufferedReader(Charsets.US_ASCII)
            try {
                val lines: MutableList<String> = mutableListOf()
                var isEmptyLineBefore = false

                while (reader.ready()) {
                    val line = reader.readText()
                    line.lines().forEach { preciseLine ->
                        when (status) {
                            State.HEADER -> {
                                // 2連続で改行が行われたらheader終了
                                if (isEmptyLineBefore) {
                                    // ここまでが Header
                                    header = processHeader(lines.toList())
                                    lines.clear()
                                    status = State.BODY
                                }
                            }

                            State.BODY -> {
                            }
                        }

                        lines.add(preciseLine)
                        isEmptyLineBefore = preciseLine.isEmpty()
                    }
                }

                val message = Message(header!!)
                onMessageReceived(message)
                return@withContext message
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
            }

            TODO()
        }
    }

    private fun processHeader(lines: List<String>): Header {
        val flatten = lines.flatMap { it.lines() }

        val firstLineSpritted = flatten[0].split(" ")
        return when (firstLineSpritted[0]) {
            Method.GET.name -> {
                requestHeader(Method.GET, flatten)
            }
            Method.PUT.name -> {
                requestHeader(Method.PUT, flatten)
            }
            else -> {
                responseHeader(flatten)
            }
        }
    }

    private fun requestHeader(method: Method, lines: List<String>): RequestHeader {
        val firstLineSplit = lines.first().split(" ")

        return RequestHeader(
            method = method,
            target = firstLineSplit[1],
            httpVersion = parseHTTPVersion(firstLineSplit[2]),
        )
    }

    private fun responseHeader(lines: List<String>): ResponseHeader {
        TODO()
    }

    private fun parseHTTPVersion(input: String): HTTPVersion {
        if (!input.startsWith("HTTP/1.")) {
            throw IllegalArgumentException("HTTP/1. Only HTTP/1.")
        }

        val minorVersion = input.elementAt(7).code

        return HTTPVersion(major = 1, minor = minorVersion)
    }
}