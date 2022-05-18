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

class Window(val draw: DrawContext.() -> Unit) {
    private var windowHandle: Long = -1L
    private var context: DirectContext? = null
    private var renderTarget: BackendRenderTarget? = null
    private var surface: Surface? = null
    private var frameBufferId: Int = 0
    private var canvas: Canvas? = null

    data class DrawContext(val canvas: Canvas, val window: Window, val unscaledDeltaTime: Long, val deltaTime: Double)

    var width = 800
    var height = 600

    var scaleX = 1.0f
    var scaleY = 1.0f

    private var lastRenderTime = -1L

    init {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        createWindow()
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
            SurfaceOrigin.TOP_LEFT,
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
        val drawContext = DrawContext(canvas, this, deltaTime, deltaTime / 1000.0)
        draw(drawContext)

        context?.flush()
        glfwSwapBuffers(windowHandle)
    }

    private fun bootstrap() {
        GL.createCapabilities()
        context = DirectContext.makeGL()

        frameBufferId = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING)

        glfwSetWindowSizeCallback(windowHandle) { _, _, _ ->
            updateDimensions()
            initializeSkia()
            drawFrame()
        }

        initializeSkia()

        while (!glfwWindowShouldClose(windowHandle)) {
            drawFrame()
            glfwPollEvents()
        }
    }
}