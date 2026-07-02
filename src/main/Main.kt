import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import server.HandleResult
import server.Handler
import server.Message
import server.Server
import server.http.Response
import server.response

class TestHandler : Handler() {
    override suspend fun onMessage(message: Message): HandleResult {
        println("[TestHandler] $message")
        return message.response(Response.string("Body!"))
    }
}

suspend fun main() {
    val server = Server(9999)
    server.addHandler(TestHandler())
    coroutineScope {
        launch { server.start() }
    }
}