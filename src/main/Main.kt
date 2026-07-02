import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import server.*
import server.http.Response
import kotlin.random.Random

class TestHandler : Handler() {
    override suspend fun onMessage(message: Message): HandleResult {
        println("[TestHandler] $message")
        if (Random.nextBoolean()) {
            return message.response(Response.string("Body!"))
        }

        return message.fail(Response.string("Failed!"))
    }
}

suspend fun main() {
    val server = Server(9999)
    server.addHandler(TestHandler())
    coroutineScope {
        launch { server.start() }
    }
}