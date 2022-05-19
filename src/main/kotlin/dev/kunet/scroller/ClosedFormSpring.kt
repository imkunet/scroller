package dev.kunet.scroller

import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

// based on https://github.com/thammin/unity-spring
class ClosedFormSpring {
    var damp = 1.0
    var mass = 1.0
    var stiff = 1.0
    var start = 0.0
    var target = 0.0
    var startVelocity = 0.0

    var value = 0.0
    var velocity = 0.0

    var totalTime = 0.0

    fun tick(deltaTime: Double) {
        totalTime += deltaTime

        val negativeStartVelocity = -startVelocity

        val dampRatio = damp / (2 * sqrt(stiff * mass))
        val angularFrequency = sqrt(stiff / mass)
        val difference = target - start

        val dampedFrequency = angularFrequency * dampRatio

        val distance: Double
        val newVelocity: Double

        if (dampRatio < 1) {
            val decay = angularFrequency * sqrt(1 - dampRatio * dampRatio)
            val e = exp(-dampedFrequency * totalTime)
            val c2 = (negativeStartVelocity + dampedFrequency * difference) / decay
            val cos = cos(decay * totalTime)
            val sin = sin(decay * totalTime)
            distance = e * (difference * cos + c2 * sin)
            newVelocity = -e * ((difference * dampedFrequency - c2 * decay) * cos + (difference * decay + c2 * dampedFrequency) * sin)
        } else if (dampRatio > 1) {
            val decay = angularFrequency / sqrt(dampRatio * dampRatio - 1)
            val z1 = -dampedFrequency - decay
            val z2 = -dampedFrequency + decay
            val e1 = exp(z1 * totalTime)
            val e2 = exp(z2 * totalTime)
            val c1 = (negativeStartVelocity - difference * z2) / (-2 * decay)
            val c2 = difference - c1
            distance = c1 * e1 + c2 * e2
            newVelocity = c1 * z1 * e1 + c2 * z2 * e2
        } else {
            val e = exp(-angularFrequency * totalTime)
            distance = e * (difference + (negativeStartVelocity + angularFrequency * difference) * totalTime)
            newVelocity = e * (negativeStartVelocity * (1 - totalTime * angularFrequency) + totalTime * difference * (angularFrequency * angularFrequency))
        }

        value = target - distance
        velocity = newVelocity
    }

    fun setTarget(target: Double, velocity: Double = this.velocity) {
        start = value
        this.target = target
        //startVelocity = velocity
        totalTime = 0.0
    }
}