package dev.kunet.scroller

import kotlin.math.floor

class Spring(
    var damping: Double,
    var stiffness: Double,

    var position: Double = 0.0,
    var target: Double = 0.0,
    var velocity: Double = 0.0,

    var stepSize: Double = 10.0,
    var leftoverStep: Double = 0.0,
) {
    var lastPoll = -1L

    fun tick() {
        if (isStopped()) {
            position = target
            velocity = 0.0
            return
        }

        val now = System.currentTimeMillis()
        if (lastPoll == -1L) lastPoll = now

        val delta = (now - lastPoll) + leftoverStep
        if (delta < stepSize) return

        val steps = floor(delta / stepSize).toInt()
        leftoverStep = delta % stepSize

        lastPoll = now

        velocity += 1.0 / stiffness * (target - position) * steps
        velocity *= (1.0 / damping).fastPow(steps)
        position += velocity * steps
    }

    fun isStopped() = position isCloseTo target && velocity isCloseTo 0.0

    fun stop() {
        position = target
        velocity = 0.0
        leftoverStep = 0.0
    }

    fun zero(zero: Double = 0.0) {
        target = zero
        stop()
    }
}
