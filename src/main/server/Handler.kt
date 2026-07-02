package server

import server.http.HTTPCode
import server.http.Request
import server.http.Response

sealed class HandleResultType {
    data class NEXT(val nextRequest: Request) : HandleResultType()
    data class RESPONSE(val response: Response, val code: HTTPCode) : HandleResultType()
    data class FAILURE(val response: Response, val code: HTTPCode) : HandleResultType()
}

fun Request.next(): HandleResult {
    return HandleResult(HandleResultType.NEXT(this), this)
}

fun Request.response(response: Response, code: HTTPCode = HTTPCode.OK): HandleResult {
    return HandleResult(HandleResultType.RESPONSE(response, code), this)
}

fun Request.fail(response: Response, code: HTTPCode = HTTPCode.INTERNAL_SERVER_ERROR): HandleResult {
    return HandleResult(HandleResultType.FAILURE(response, code), this)
}

class HandleResult(val type: HandleResultType, val processedRequest: Request)

abstract class Handler {
    abstract suspend fun onRequest(request: Request): HandleResult
}