package dev.kunet.scroller

import org.jetbrains.skia.*
import org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT
import org.lwjgl.glfw.GLFW.GLFW_RELEASE

fun main() {
    val spring = ClosedFormSpring().let {
        it.mass = 0.5
        it.stiff = 169.0
        it.damp = 26.0
        it.target = 350.0

        it
    }

    val typeface = Typeface.makeFromData(Data.makeFromBytes(fetchResource("fonts", "Inter-Regular.ttf")))
    val regularFont = Font(typeface)

    var lastUpdatedTextTime = 0L
    var lastUpdatedText = TextLine.make("0 FPS", regularFont)

    val backgroundPaint = Paint().setARGB(0xFF, 0x30, 0x30, 0x30)
    val redPaint = Paint().setARGB(0xFF, 0xFF, 0x00, 0x00)
    val whitePaint = Paint().setARGB(0xFF, 0xFF, 0xFF, 0xFF)

    Window {
        onDraw {
            spring.tick(deltaTime)

            canvas.drawRect(
                Rect(0.0f, 0.0f, window.width.toFloat(), window.height.toFloat()),
                backgroundPaint
            )

            canvas.drawRect(
                Rect.makeXYWH(spring.value.toFloat(), 0.0f, 100.0f, 100.0f),
                redPaint
            )

            if (currentTime - lastUpdatedTextTime > 1000) {
                lastUpdatedTextTime = currentTime
                lastUpdatedText.close()
                lastUpdatedText = TextLine.make("${window.profiler.getFramerate()} FPS", regularFont)
            }

            canvas.drawTextLine(lastUpdatedText, 0f, lastUpdatedText.height, whitePaint)
        }

        onMouseButton {
            if (button != GLFW_MOUSE_BUTTON_LEFT || action != GLFW_RELEASE) return@onMouseButton

            spring.setTarget(mouseX)
        }
    }
}
