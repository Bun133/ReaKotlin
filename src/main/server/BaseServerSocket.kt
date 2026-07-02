package server

import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.ServerSocket as JavaServerSocket

abstract class BaseServerSocket<M>(port: Int) {
    private val socket = JavaServerSocket(port)

    // Accepting new connection in suspend.
    private suspend fun accept(): Socket = withContext(Dispatchers.IO) {
        socket.accept()
    }

    // handling input stream from socket, triggering other process.
    abstract suspend fun handleStream(scope: CoroutineScope, inputStream: InputStream, outputStream: OutputStream): M

    fun start(scope: CoroutineScope) {
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        scope.launch(Dispatchers.IO) {
            try {
                while (isActive) {
                    val newSocket = accept()
                    println("New socket accepted: ${newSocket.remoteSocketAddress}")
                    supervisorScope {
                        inputStream = newSocket.getInputStream()
                        outputStream = newSocket.getOutputStream()
                        handleStream(this, inputStream, outputStream)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (!socket.isClosed) {
                    socket.close()
                }
                inputStream?.close()
                outputStream?.close()
            }
        }
    }
}