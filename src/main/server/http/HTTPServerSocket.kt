package server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import server.header.HTTPVersion
import server.header.Method
import server.header.RequestHeader
import server.header.ResponseHeader
import server.http.Request
import java.io.InputStream
import java.io.OutputStream

open class HTTPServerSocket(port: Int) : BaseServerSocket<Request>(port) {
    // on Message Received
    open suspend fun onRequest(request: Request, outputStream: OutputStream) {}

    override suspend fun handleStream(
        scope: CoroutineScope,
        inputStream: InputStream,
        outputStream: OutputStream
    ): Request {
        return withContext(Dispatchers.IO) {
            val reader = inputStream.bufferedReader(Charsets.US_ASCII)
            try {
                val headerLines = mutableListOf<String>()
                while (true){
                    if (reader.ready()){
                        val line = reader.readLine()
                        if (line.isNotEmpty()){
                            headerLines.add(line)
                        } else {
                            // empty line, end of the header
                            break
                        }
                    }
                }
                val header = processHeader(headerLines)

                // TODO 終了検知
                // val body = reader.readAllAsString()

                val message = Request(header, ByteArray(1))
                onRequest(message, outputStream)
                return@withContext message
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.close()
            }

            TODO()
        }
    }

    private fun processHeader(lines: List<String>): RequestHeader {
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
                TODO()
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
            keyValue = keyValue
        )
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