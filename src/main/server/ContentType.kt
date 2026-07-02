package server

enum class MIME(val value: String) {
    JSON("application/json"),
    HTML("text/html"),
    TEXT("text/plain"),
    CSV("text/csv"),
}

data class ContentType(val mime: MIME, val parameters: Map<String, String> = mapOf()) {

}