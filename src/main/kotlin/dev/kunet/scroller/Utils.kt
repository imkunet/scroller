package dev.kunet.scroller

fun fetchResource(key: String, path: String, errorMessage: String = "Failed to fetch resource $key/$path") =
    Window::class.java.getResourceAsStream("/$key/$path")?.readAllBytes() ?: throw IllegalStateException(errorMessage)
