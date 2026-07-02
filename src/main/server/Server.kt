package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    internal suspend fun processMessage(message: Message): Response {
        var targetMessage = message
        for (handler in handlers) {
            val result = handler.onMessage(targetMessage)
            when (result.type) {
                is HandleResultType.NEXT -> {
                    targetMessage = result.type.nextMessage
                }
                is HandleResultType.RESPONSE -> {
                    return result.type.response
                }
                is HandleResultType.FAILURE -> {
                    // Give up
                    throw Error("Handler Failed: ${result.type}")
                }
            }
        }

        // No Response From Handlers
        return defaultResponse()
    }

    private fun defaultResponse():Response{
        return Response.string("No Matching Handler", HTTPCode.INTERNAL_SERVER_ERROR)
    }
}

private class ServerSocket(port: Int, val serverInstance: Server): HTTPServerSocket(port) {
    override suspend fun onMessageReceived(message: Message, outputStream: OutputStream) {
        val response = serverInstance.processMessage(message)
        this.writeResponse(response, outputStream)
    }

    private fun writeResponse(response: Response, outputStream: OutputStream) {
        val statusLine = "${response.header.httpVersion} ${response.header.code.code}"
        val headerLines = response.header.keyValue.map { "${it.key}: ${it.value}" }.joinToString("\r\n")
        val separatorLine = "\r\n"
        val header = listOf(statusLine, headerLines, separatorLine).joinToString("\r\n")
        val headerBytes = header.toByteArray()
        val bodyLines = response.body

        val allLines = listOf(headerBytes, bodyLines)
        val byteBuffer = ByteBuffer.allocate(allLines.sumOf { it.size })
        allLines.forEach { byteBuffer.put(it) }
        byteBuffer.flip()

        outputStream.write(byteBuffer.array())
    }
}