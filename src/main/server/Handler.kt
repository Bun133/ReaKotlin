package server

enum class HandleResultType {
    SUCCESS,
    FAILURE,
}

class HandleResult(val type: HandleResultType, val nextMessage: Message) {
    companion object {
        fun ok(message: Message): HandleResult {
            return HandleResult(HandleResultType.SUCCESS, message)
        }

        fun fail(message: Message): HandleResult {
            return HandleResult(HandleResultType.FAILURE, message)
        }
    }
}

abstract class Handler {
    abstract suspend fun onMessage(message: Message)
}