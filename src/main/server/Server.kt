package server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Server(val port: Int) {
    private val serverSocket: HTTPServerSocket = HTTPServerSocket(port)

    suspend fun start() {
        withContext(Dispatchers.Default) {
            serverSocket.start(this)
        }
    }
}