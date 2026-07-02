package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import server.http.HTTPCode
import server.http.Request
import server.http.Response
import java.io.OutputStream
import java.nio.ByteBuffer

class Server(port: Int) {
    private val serverSocket: HTTPServerSocket = ServerSocket(port, this)
    private val handlers = mutableListOf<Handler>()

    suspend fun start() {
        withContext(Dispatchers.Default) {
            serverSocket.start(this)
        }
    }

    fun addHandler(handler: Handler) {
        handlers.add(handler)
    }

    internal suspend fun processMessage(request: Request): HandleResult {
        var targetRequest = request
        for (handler in handlers) {
            val result = handler.onRequest(targetRequest)
            when (result.type) {
                is HandleResultType.NEXT -> {
                    targetRequest = result.type.nextRequest
                }

                is HandleResultType.RESPONSE -> {
                    return result
                }

                is HandleResultType.FAILURE -> {
                    return result
                }
            }
        }

        // No Response From Handlers
        return defaultHandleResult(request)
    }

    private fun defaultHandleResult(request: Request): HandleResult {
        return request.fail(Response.string("No Matching Handler"))
    }
}

private class ServerSocket(port: Int, val serverInstance: Server) : HTTPServerSocket(port) {
    override suspend fun onRequest(request: Request, outputStream: OutputStream) {
        val result = serverInstance.processMessage(request)
        when (result.type) {
            is HandleResultType.NEXT -> {
                throw IllegalArgumentException("HandleResultType.NEXT cannot be the final response type")
            }

            is HandleResultType.RESPONSE -> {
                this.writeResult(result.type, result.type.response, result.type.code, outputStream)
            }

            is HandleResultType.FAILURE -> {
                this.writeResult(result.type, result.type.response, result.type.code, outputStream)
            }
        }
    }

    private fun writeResult(type: HandleResultType, response: Response, code: HTTPCode, outputStream: OutputStream) {
        val statusLine = "${response.header.httpVersion} ${code.code}"
        val headerLines = response.header.toHeaderLines()
        val separatorLine = "\r\n"

        val headingLines = mutableListOf<String>()
        headingLines.add(statusLine)
        headingLines.addAll(headerLines)
        headingLines.add(separatorLine)
        val header = headingLines.joinToString("\r\n")
        println(header)

        val headerBytes = header.toByteArray()
        val bodyLines = response.body

        val allLines = listOf(headerBytes, bodyLines)
        val byteBuffer = ByteBuffer.allocate(allLines.sumOf { it.size })
        allLines.forEach { byteBuffer.put(it) }
        byteBuffer.flip()

        outputStream.write(byteBuffer.array())
    }
}