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
            return request.next()
        }

        if (Random.nextBoolean()) {
            return request.response(Response.string("Body!"))
        }

        return request.fail(Response.string("Failed!"))
    }
}

class HTMLHandler: Handler() {
    val html = "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<body>\n" +
            "    <h1>Test</h1>\n" +
            "    <p>Hello World</p>\n" +
            "</body>\n" +
            "</html>"

    override suspend fun onRequest(request: Request): HandleResult {
        println("[HTMLHandler] $request")
        return request.response(Response.html(html))
    }
}

suspend fun main() {
    val server = Server(9999)
//    server.addHandler(TestHandler())
    server.addHandler(HTMLHandler())

    coroutineScope {
        launch { server.start() }
    }
}