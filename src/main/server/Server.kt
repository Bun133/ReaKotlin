package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import server.header.ResponseHeader
import server.http.HTTPCode
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

    internal suspend fun processMessage(message: Message): HandleResult {
        var targetMessage = message
        for (handler in handlers) {
            val result = handler.onMessage(targetMessage)
            when (result.type) {
                is HandleResultType.NEXT -> {
                    targetMessage = result.type.nextMessage
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
        return defaultHandleResult(message)
    }

    private fun defaultHandleResult(message: Message): HandleResult {
        return message.fail(Response.string("No Matching Handler"))
    }
}

private class ServerSocket(port: Int, val serverInstance: Server) : HTTPServerSocket(port) {
    override suspend fun onMessageReceived(message: Message, outputStream: OutputStream) {
        val result = serverInstance.processMessage(message)
        this.writeResult(result, outputStream)
    }

    private fun writeResult(result: HandleResult, outputStream: OutputStream) {
        var responseHeader: ResponseHeader? = null
        var response: Response? = null
        var code: HTTPCode? = null

        if (result.type is HandleResultType.RESPONSE) {
            responseHeader = result.type.response.header
            response = result.type.response
            code = result.type.code
        } else if (result.type is HandleResultType.FAILURE) {
            responseHeader = result.type.response.header
            response = result.type.response
            code = result.type.code
        }

        if (responseHeader == null || response == null || code == null) {
            throw IllegalArgumentException("HandleResultType ${result.type} cannot be written to output stream")
        }

        val statusLine = "${responseHeader.httpVersion} ${code.code}"
        val headerLines = responseHeader.keyValue.map { "${it.key}: ${it.value}" }.joinToString("\r\n")
        val separatorLine = "\r\n"
        val header = listOf(statusLine, headerLines, separatorLine).filter { it.isNotEmpty() }.joinToString("\r\n")
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