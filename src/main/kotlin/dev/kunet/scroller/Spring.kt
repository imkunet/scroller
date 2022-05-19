package dev.kunet.scroller

import kotlin.math.floor

class Spring(
    var damping: Double,
    var stiffness: Double,

    var position: Double = 0.0,
    var target: Double = 0.0,
    var velocity: Double = 0.0,

    var stepSize: Double = 100.0,
    var leftoverStep: Double = 0.0,
) {
    fun tick(deltaTime: Double) {
        val delta = deltaTime + leftoverStep

        if (delta < stepSize) {
            leftoverStep += delta
            return
        }

        val steps = floor(delta / stepSize).toInt()

        leftoverStep = delta % stepSize

        if (isStopped()) {
            position = target
            velocity = 0.0
            return
        }

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
