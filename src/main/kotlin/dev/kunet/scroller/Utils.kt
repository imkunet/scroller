package dev.kunet.scroller

import kotlin.math.abs

fun fetchResource(key: String, path: String, errorMessage: String = "Failed to fetch resource $key/$path") =
    Window::class.java.getResourceAsStream("/$key/$path")?.readAllBytes() ?: throw IllegalStateException(errorMessage)

fun Double.fastPow(x: Int): Double {
    return when (val absolute = abs(x)) {
        x -> this.internalPow(absolute)
        else -> 1 / this.internalPow(absolute)
    }
}

private fun Double.internalPow(x: Int): Double {
    var sum = 1.0
    for (i in 0 until x) sum *= this
    return sum
}

infix fun Double.isCloseTo(other: Double) = abs(this - other) < 0.01
