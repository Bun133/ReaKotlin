package server

class ServerSocket(port: Int, val serverInstance: Server): HTTPServerSocket(port) {
    override fun onMessageReceived(message: Message) {

    }
}