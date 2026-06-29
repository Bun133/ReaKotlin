package server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    val keyValue: Map<String, String>
    val rawHeader: List<String>
}

data class ResponseHeader(
    val status: Int,
    val headers: Map<String, String>,
    override val httpVersion: HTTPVersion,
    override val rawHeader: List<String>,
    override val keyValue: Map<String, String>
) : Header

data class RequestHeader(
    override val httpVersion: HTTPVersion,
    val target: String,
    val method: Method,
    override val rawHeader: List<String>,
    override val keyValue: Map<String, String>,
) : Header

data class Message(
    val header: Header
)

open class HTTPServerSocket(port: Int) : BaseServerSocket<Message>(port) {
    private var status: State = State.HEADER
    private var header: Header? = null

    // on Message Received
    open suspend fun onMessageReceived(message: Message) {}

    override suspend fun handleStream(scope: CoroutineScope, inputStream: InputStream): Message {
        return withContext(Dispatchers.IO) {
            val reader = inputStream.bufferedReader(Charsets.US_ASCII)
            try {
                val lines: MutableList<String> = mutableListOf()
                while (reader.ready()) {
                    val line = reader.readLine()
                    when (status) {
                        State.HEADER -> {
                            // 改行が行われたらheader終了
                            if (line.isEmpty()) {
                                // ここまでが Header
                                header = processHeader(lines.toList())
                                lines.clear()
                                status = State.BODY
                            }
                        }

                        State.BODY -> {
                        }
                    }

                    lines.add(line)
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

            Method.POST.name -> {
                requestHeader(Method.POST, flatten)
            }

            else -> {
                responseHeader(flatten)
            }
        }
    }

    private fun requestHeader(method: Method, lines: List<String>): RequestHeader {
        val firstLineSplit = lines.first().split(" ")
        val remaining = lines.subList(1, lines.size)
        val keyValue = parseKeyValueHeader(remaining)

        return RequestHeader(
            method = method,
            target = firstLineSplit[1],
            httpVersion = parseHTTPVersion(firstLineSplit[2]),
            rawHeader = lines,
            keyValue = keyValue
        )
    }

    private fun responseHeader(lines: List<String>): ResponseHeader {
        TODO()
    }

    private fun parseHTTPVersion(input: String): HTTPVersion {
        if (!input.startsWith("HTTP/1.")) {
            throw IllegalArgumentException("HTTP/1. Only HTTP/1.")
        }

        val minorVersion = input.elementAt(7).digitToInt()

        return HTTPVersion(major = 1, minor = minorVersion)
    }

    private fun parseKeyValueHeader(headers: List<String>): Map<String, String> {
        return headers.associate {
            val splitIndex = it.indexOf(':')
            val key = it.substring(0, splitIndex)
            val value = it.substring(splitIndex + 1).trimStart()
            Pair(key, value)
        }
    }
}