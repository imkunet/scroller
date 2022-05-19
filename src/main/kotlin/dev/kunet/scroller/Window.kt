package dev.kunet.scroller

import org.jetbrains.skia.*
import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.system.MemoryUtil.NULL
import kotlin.math.roundToInt

private val os = System.getProperty("os.name").lowercase()
private val isMac = os.contains("mac") || os.contains("darwin")

class Window(builder: Window.() -> Unit) {
    val profiler = Profiler()

    private var onDraw: DrawContext.() -> Unit = {}
    private var onMouseButton: MouseButtonEventContext.() -> Unit = {}
    private var onMouseMove: MouseMoveEventContext.() -> Unit = {}

    private var windowHandle: Long = -1L
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var frameBufferId: Int = 0
    private var canvas: Canvas? = null

    data class DrawContext(
        val canvas: Canvas,
        val window: Window,
        val currentTime: Long,
        val unscaledDeltaTime: Long,
        val deltaTime: Double,
    )

    data class MouseButtonEventContext(
        val button: Int,
        val action: Int,
        val mods: Int,
    )

    data class MouseMoveEventContext(
        val x: Double,
        val y: Double,
        val deltaX: Double,
        val deltaY: Double,
    )

    fun onDraw(draw: DrawContext.() -> Unit) {
        onDraw = draw
    }

    fun onMouseButton(event: MouseButtonEventContext.() -> Unit) {
        onMouseButton = event
    }

    fun onMouseMove(event: MouseMoveEventContext.() -> Unit) {
        onMouseMove = event
    }

    var width = 800
    var height = 600

    var scaleX = 1.0f
    var scaleY = 1.0f

    var mouseX = 0.0
    var mouseY = 0.0

    private var lastRenderTime = -1L

    init {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        createWindow()
        builder()
        bootstrap()

        // cleanup
        glfwFreeCallbacks(windowHandle)
        glfwDestroyWindow(windowHandle)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    private fun createWindow() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        windowHandle = glfwCreateWindow(width, height, "Scroller", NULL, NULL)
        if (windowHandle == NULL || windowHandle == -1L) throw RuntimeException("Failed to create the GLFW window")

        glfwSetKeyCallback(windowHandle) { _, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(windowHandle, true)
            }
        }

        glfwSetWindowSizeCallback(windowHandle) { _, _, _ ->
            updateDimensions()
            initializeSkia()
            drawFrame()
        }

        glfwSetCursorPosCallback(windowHandle) { _, x, y ->
            val oldX = mouseX
            val oldY = mouseY

            if (isMac) {
                mouseX = x
                mouseY = y
                return@glfwSetCursorPosCallback
            }

            mouseX = x / scaleX
            mouseY = y / scaleY

            onMouseMove(MouseMoveEventContext(mouseX, mouseY, oldX - mouseX, oldY - mouseY))
        }

        glfwSetMouseButtonCallback(windowHandle) { _, button, action, mods ->
            onMouseButton(MouseButtonEventContext(button, action, mods))
        }

        updateDimensions()

        glfwMakeContextCurrent(windowHandle)
        glfwSwapInterval(1) // vertical sync
        glfwShowWindow(windowHandle)
    }

    private fun updateDimensions() {
        val width = IntArray(1)
        val height = IntArray(1)
        glfwGetFramebufferSize(windowHandle, width, height)

        val scaleX = FloatArray(1)
        val scaleY = FloatArray(1)
        glfwGetWindowContentScale(windowHandle, scaleX, scaleY)

        this.width = (width[0] / scaleX[0]).roundToInt()
        this.height = (height[0] / scaleY[0]).roundToInt()

        this.scaleX = scaleX[0]
        this.scaleY = scaleY[0]
    }

    private fun initializeSkia() {
        renderTarget?.close()
        surface?.close()

        val context = context ?: return

        val renderTarget = BackendRenderTarget.makeGL(
            width,
            height,
            0,
            8,
            frameBufferId,
            FramebufferFormat.GR_GL_RGBA8,
        )

        val surface = Surface.makeFromBackendRenderTarget(
            context,
            renderTarget,
            SurfaceOrigin.BOTTOM_LEFT,
            SurfaceColorFormat.RGBA_8888,
            ColorSpace.sRGB
        )

        this.renderTarget = renderTarget
        this.surface = surface

        this.canvas = surface?.canvas
    }

    private fun drawFrame() {
        if (lastRenderTime == -1L) lastRenderTime = System.currentTimeMillis()
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastRenderTime
        lastRenderTime = currentTime

        val canvas = canvas ?: return
        val drawContext = DrawContext(canvas, this, currentTime, deltaTime, deltaTime / 1000.0)
        onDraw(drawContext)

        context?.flush()
        glfwSwapBuffers(windowHandle)

        profiler.updateFramerate()
    }

    private fun bootstrap() {
        GL.createCapabilities()
        context = DirectContext.makeGL()

        frameBufferId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)

        initializeSkia()

        while (!glfwWindowShouldClose(windowHandle)) {
            drawFrame()
            glfwPollEvents()
        }
    }
}
