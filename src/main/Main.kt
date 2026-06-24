import kotlinx.coroutines.coroutineScope
import server.HTTPServerSocket

suspend fun main() {
    val serverSocket = HTTPServerSocket(9999)
    coroutineScope {
        serverSocket.start(this)
    }
}