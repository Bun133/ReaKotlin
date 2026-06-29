package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    internal suspend fun processMessage(message: Message) {
        var targetMessage = message
        for (handler in handlers) {
            val result = handler.onMessage(targetMessage)
            when (result.type) {
                HandleResultType.SUCCESS -> {
                    targetMessage = result.nextMessage
                }
                HandleResultType.FAILURE -> {
                    // Give up
                    println("[Server] Handler Failed: $handler")
                }
            }
        }
    }
}

private class ServerSocket(port: Int, val serverInstance: Server): HTTPServerSocket(port) {
    override suspend fun onMessageReceived(message: Message) {
        serverInstance.processMessage(message)
    }
}