package server

import server.http.HTTPCode
import server.http.Response

sealed class HandleResultType {
    data class NEXT(val nextMessage: Message) : HandleResultType()
    data class RESPONSE(val response: Response, val code: HTTPCode) : HandleResultType()
    data class FAILURE(val response: Response, val code: HTTPCode) : HandleResultType()
}

fun Message.next(): HandleResult {
    return HandleResult(HandleResultType.NEXT(this), this)
}

fun Message.response(response: Response, code: HTTPCode = HTTPCode.OK): HandleResult {
    return HandleResult(HandleResultType.RESPONSE(response, code), this)
}

fun Message.fail(response: Response, code: HTTPCode = HTTPCode.INTERNAL_SERVER_ERROR): HandleResult {
    return HandleResult(HandleResultType.FAILURE(response, code), this)
}

class HandleResult(val type: HandleResultType, val processedMessage: Message)

abstract class Handler {
    abstract suspend fun onMessage(message: Message): HandleResult
}