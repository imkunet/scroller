package dev.kunet.scroller

import org.jetbrains.skia.*

fun main() {
    val spring = ClosedFormSpring().let {
        it.mass = 1.0
        it.stiff = 60.0
        it.damp = 6.0
        it.target = 350.0

        it
    }

    Window {
        spring.tick(deltaTime)

        canvas.drawRect(
            Rect(0.0f, 0.0f, window.width.toFloat(), window.height.toFloat()),
            Paint().setARGB(0xFF, 0x00, 0x00, 0x00)
        )

        canvas.drawRect(
            Rect.makeXYWH(spring.value.toFloat(), 0.0f, 100.0f, 100.0f),
            Paint().setARGB(0xFF, 0xFF, 0x00, 0x00)
        )
    }
}
