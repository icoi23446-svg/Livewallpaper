package com.example.livewallpaper

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return MultiPatternEngine()
    }

    inner class MultiPatternEngine : Engine() {
        private val paint = Paint()
        private val backgroundPaint = Paint()
        private var running = true
        private var thread: Thread? = null

        private var pattern = 0
        private var color1 = Color.BLUE
        private var color2 = Color.RED
        private var speed = 5

        init {
            backgroundPaint.style = Paint.Style.FILL
            loadPrefs()
        }

        private fun loadPrefs() {
            val prefs = getSharedPreferences("live_prefs", MODE_PRIVATE)
            pattern = prefs.getInt("pattern", 0)
            color1 = Color.parseColor(prefs.getString("color1", "#0000FF") ?: "#0000FF")
            color2 = Color.parseColor(prefs.getString("color2", "#FF0000") ?: "#FF0000")
            speed = prefs.getInt("speed", 5)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) {
                running = true
                startDrawing()
            } else {
                running = false
                thread?.join(200)
            }
        }

        private fun startDrawing() {
            if (thread?.isAlive == true) return
            thread = Thread {
                while (running) {
                    drawFrame()
                    try {
                        Thread.sleep(40L)
                    } catch (_: InterruptedException) {
                    }
                }
            }
            thread?.start()
        }

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    drawPattern(canvas)
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private var t = 0.0

        private fun drawPattern(canvas: Canvas) {
            val w = canvas.width
            val h = canvas.height
            canvas.drawColor(Color.BLACK)

            when (pattern) {
                0 -> { // Gradient Shift
                    val shader = LinearGradient(
                        0f, 0f, w.toFloat(), h.toFloat(),
                        color1, color2, Shader.TileMode.MIRROR
                    )
                    paint.shader = shader
                    canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
                }
                1 -> { // Aurora / Nebula effect
                    paint.shader = null
                    for (i in 0..50) {
                        paint.color = Color.argb(
                            50,
                            Random.nextInt(256),
                            Random.nextInt(256),
                            Random.nextInt(256)
                        )
                        val x = Random.nextInt(w).toFloat()
                        val y = Random.nextInt(h).toFloat()
                        val r = Random.nextInt(50, 200).toFloat()
                        canvas.drawCircle(x, y, r, paint)
                    }
                }
                2 -> { // Blobs moving
                    paint.shader = null
                    val blobCount = 10
                    for (i in 0 until blobCount) {
                        paint.color = if (i % 2 == 0) color1 else color2
                        val x = ((w / 2) + (100 * sin((t + i) / speed))).toFloat()
                        val y = ((h / 2) + (100 * sin((t + i * 2) / speed))).toFloat()
                        canvas.drawCircle(x, y, 80f, paint)
                    }
                }
                3 -> { // Particles
                    paint.shader = null
                    paint.color = color1
                    for (i in 0..100) {
                        val x = Random.nextInt(w).toFloat()
                        val y = ((h / 2) + (50 * sin((t + i) / speed))).toFloat()
                        canvas.drawCircle(x, y, 5f, paint)
                    }
                }
                4 -> { // Conic Spin
                    val cx = w / 2f
                    val cy = h / 2f
                    val radius = (w.coerceAtMost(h) / 2).toFloat()
                    val sweep = ((t * speed) % 360).toFloat()
                    paint.shader = SweepGradient(cx, cy, color1, color2)
                    canvas.drawArc(
                        RectF(cx - radius, cy - radius, cx + radius, cy + radius),
                        sweep, 270f, true, paint
                    )
                }
            }

            t += 0.1
        }
    }
}
