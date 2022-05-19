package dev.kunet.scroller

import org.jetbrains.skia.*
import org.lwjgl.glfw.GLFW.*

fun main() {
    val spring = Spring(3.0, 1.5)

    val typeface = Typeface.makeFromData(Data.makeFromBytes(fetchResource("fonts", "Inter-Regular.ttf")))
    val regularFont = Font(typeface)

    val semiBoldTypeface = Typeface.makeFromData(Data.makeFromBytes(fetchResource("fonts", "Inter-SemiBold.ttf")))
    val semiBoldFont = Font(semiBoldTypeface, 16f)

    var lastUpdatedTextTime = 0L
    var lastUpdatedText = TextLine.make("0 FPS", regularFont)

    val backgroundPaint = Paint().setARGB(0xFF, 0x30, 0x30, 0x30)
    val whitePaint = Paint().setARGB(0xFF, 0xFF, 0xFF, 0xFF)
    val blackPaint = Paint().setARGB(0xFF, 0x00, 0x00, 0x00)

    val helloWorldText = TextLine.make("Hello World", semiBoldFont)
    val helloWorldHeight = helloWorldText.height

    var containerX = 0.0
    var containerY = 0.0

    var containerHeight: Double

    fun getContainerY() = containerY + spring.position

    Window {
        var leftDown = false
        var cumulativeY = 0.0
        var pressTime = -1L

        onDraw {
            spring.tick(unscaledDeltaTime.toDouble())

            canvas.drawRect(
                Rect(0.0f, 0.0f, window.width.toFloat(), window.height.toFloat()),
                backgroundPaint
            )

            var offsetHeight = 0f

            while (offsetHeight < window.height * 2) {
                canvas.drawTextLine(
                    helloWorldText,
                    offsetHeight / 2 + containerX.toFloat(),
                    offsetHeight + getContainerY().toFloat(),
                    whitePaint
                )
                offsetHeight += helloWorldHeight * 2
            }

            containerHeight = offsetHeight.toDouble()

            /*canvas.drawRRect(
                RRect.makeXYWH(
                    window.width.toFloat() - 20,
                    containerY.toFloat() / containerHeight.toFloat() * window.height,
                    20f,
                    window.height.toFloat() / containerHeight.toFloat(),
                    10f
                ),
                whitePaint
            )*/

            if (currentTime - lastUpdatedTextTime > 50) {
                lastUpdatedTextTime = currentTime
                lastUpdatedText.close()
                lastUpdatedText =
                    TextLine.make(
                        "${window.profiler.getFramerate()} FPS $leftDown ${spring.position} ${getContainerY()}",
                        regularFont
                    )
            }

            canvas.drawTextLine(lastUpdatedText, 0f, lastUpdatedText.height, whitePaint)
        }

        onMouseButton {
            if (button != GLFW_MOUSE_BUTTON_LEFT) return@onMouseButton

            if (action == GLFW_RELEASE) {
                leftDown = false

                val deltaTime = System.currentTimeMillis() - pressTime
                val velocity = (cumulativeY / deltaTime) * 100

                spring.position = 0.0
                spring.target = -velocity
            }

            if (action == GLFW_PRESS) {
                leftDown = true
                cumulativeY = 0.0
                pressTime = System.currentTimeMillis()
                containerY = getContainerY()
                spring.zero()
            }
        }

        onMouseMove {
            if (!leftDown) return@onMouseMove

            containerY -= deltaY
            cumulativeY += deltaY
        }
    }
}
