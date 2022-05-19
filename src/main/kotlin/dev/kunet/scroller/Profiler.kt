package dev.kunet.scroller

class Profiler {
    private val times = mutableListOf<Long>()
    private var framerate = 0

    fun updateFramerate() {
        val now = System.currentTimeMillis()
        times.add(now)

        while (true) {
            val first = times.firstOrNull() ?: break
            if (now - first > 1000) times.removeAt(0)
            else break
        }

        framerate = times.size
    }

    fun getFramerate() = framerate
}
