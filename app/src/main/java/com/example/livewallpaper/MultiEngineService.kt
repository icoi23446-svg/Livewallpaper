package com.example.livewallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import kotlin.math.sin
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine = FancyEngine()

    inner class FancyEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var running = true
        private var angle = 0f

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
                if (running) handler.postDelayed(this, 30)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) handler.post(drawRunner) else handler.removeCallbacks(drawRunner)
        }

        private fun drawFrame() {
            val canvas = surfaceHolder.lockCanvas() ?: return
            try {
                val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
                val pattern = prefs.getString("pattern", "Animated Gradient") ?: "Animated Gradient"
                val speed = prefs.getInt("speed", 5)

                val w = canvas.width
                val h = canvas.height
                angle += 0.02f * speed

                when (pattern) {
                    "Animated Gradient" -> drawAnimatedGradient(canvas, w, h)
                    "Color Cycle" -> drawColorCycle(canvas, w, h)
                    "Particles" -> drawParticles(canvas, w, h)
                    "Waves" -> drawWaves(canvas, w, h)
                    else -> drawAnimatedGradient(canvas, w, h)
                }
            } finally {
                surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        private fun drawAnimatedGradient(canvas: Canvas, w: Int, h: Int) {
            val time = System.currentTimeMillis() % 5000L
            val fraction = (time / 5000f)
            val startColor = Color.HSVToColor(floatArrayOf(360f * fraction, 1f, 1f))
            val endColor = Color.HSVToColor(floatArrayOf(360f * (1 - fraction), 1f, 1f))

            val shader = LinearGradient(
                0f, 0f, w.toFloat(), h.toFloat(),
                startColor, endColor,
                Shader.TileMode.MIRROR
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
        }

        private fun drawColorCycle(canvas: Canvas, w: Int, h: Int) {
            val color = Color.HSVToColor(floatArrayOf((angle * 50) % 360, 1f, 1f))
            canvas.drawColor(color)
        }

        private fun drawParticles(canvas: Canvas, w: Int, h: Int) {
            canvas.drawColor(Color.BLACK)
            paint.color = Color.WHITE
            for (i in 0..100) {
                val x = Random.nextInt(w)
                val y = ((h/2) + sin(angle + i) * h/2).toFloat()
                canvas.drawCircle(x.toFloat(), y, 4f, paint)
            }
        }

        private fun drawWaves(canvas: Canvas, w: Int, h: Int) {
            canvas.drawColor(Color.BLACK)
            paint.color = Color.CYAN
            paint.strokeWidth = 4f
            for (i in 0..w step 20) {
                val y = (h/2 + sin((i + angle * 50) * 0.05) * 100).toFloat()
                canvas.drawLine(i.toFloat(), y, i.toFloat(), h.toFloat(), paint)
            }
        }
    }
}
