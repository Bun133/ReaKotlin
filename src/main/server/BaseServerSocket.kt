package server

import kotlinx.coroutines.*
import java.io.InputStream
import java.net.Socket
import java.net.ServerSocket as JavaServerSocket

abstract class BaseServerSocket<M>(port: Int) {
    private val socket = JavaServerSocket(port)

    // Accepting new connection in suspend.
    private suspend fun accept(): Socket = withContext(Dispatchers.IO) {
        socket.accept()
    }

    // handling input stream from socket, triggering other process.
    abstract suspend fun handleStream(scope: CoroutineScope, inputStream: InputStream): M

    fun start(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val newSocket = accept()
                    println("New socket accepted: ${newSocket.remoteSocketAddress}")
                    supervisorScope {
                        handleStream(this, newSocket.getInputStream())
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (!socket.isClosed) {
                    socket.close()
                }
            }
        }
    }
}