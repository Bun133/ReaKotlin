import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import server.*
import server.http.Request
import server.http.Response
import kotlin.random.Random

class TestHandler : Handler() {
    override suspend fun onRequest(request: Request): HandleResult {
        println("[TestHandler] $request")
        if (Random.nextBoolean()) {
            return request.response(Response.string("Body!"))
        }

        return request.fail(Response.string("Failed!"))
    }
}

suspend fun main() {
    val server = Server(9999)
    server.addHandler(TestHandler())
    coroutineScope {
        launch { server.start() }
    }
}