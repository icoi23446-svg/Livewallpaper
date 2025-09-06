package com.example.livewallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.*
import android.view.SurfaceHolder
import android.content.Context
import kotlin.math.*
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return FancyEngine(this)
    }

    inner class FancyEngine(private val context: Context) : Engine() {
        private val paint = Paint()
        private val gradientPaint = Paint()
        private val matrix = Matrix()
        private var handler = android.os.Handler()
        private var running = true

        // الإعدادات من SharedPreferences
        private val prefs = context.getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        private var pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
        private var colorName = prefs.getString("color", "أزرق") ?: "أزرق"
        private var direction = prefs.getString("direction", "يمين") ?: "يمين"
        private var effect = prefs.getString("effect", "بدون") ?: "بدون"
        private var speed = prefs.getInt("speed", 5)
        private var size = prefs.getInt("size", 50)
        private var density = prefs.getInt("density", 5)

        private val particles = mutableListOf<Particle>()

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
                if (running) handler.postDelayed(this, 30)
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) {
                handler.post(drawRunner)
            } else {
                handler.removeCallbacks(drawRunner)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            running = false
            handler.removeCallbacks(drawRunner)
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
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }

        private fun drawPattern(canvas: Canvas) {
            when (pattern) {
                "تدرج لوني" -> drawGradient(canvas)
                "تغير لون" -> drawColorShift(canvas)
                "جسيمات" -> drawParticles(canvas)
                "موجات" -> drawWaves(canvas)
                else -> drawGradient(canvas)
            }
        }

        private fun drawGradient(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val gradient = LinearGradient(
                0f, 0f, if (direction == "يمين") w else 0f, if (direction == "أسفل") h else 0f,
                intArrayOf(Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA),
                null,
                Shader.TileMode.MIRROR
            )
            gradientPaint.shader = gradient
            canvas.drawRect(0f, 0f, w, h, gradientPaint)
        }

        private fun drawColorShift(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val time = System.currentTimeMillis() % 10000L / 10000f
            val color = Color.HSVToColor(floatArrayOf(time * 360f, 1f, 1f))
            canvas.drawColor(color)
        }

        private fun drawParticles(canvas: Canvas) {
            val w = canvas.width
            val h = canvas.height

            if (particles.isEmpty()) {
                repeat(density * 20) {
                    particles.add(Particle.random(w, h, size))
                }
            }

            canvas.drawColor(Color.BLACK)

            paint.color = colorFromName(colorName)
            paint.style = Paint.Style.FILL

            for (p in particles) {
                canvas.drawCircle(p.x, p.y, size / 10f, paint)
                p.x += p.vx * speed
                p.y += p.vy * speed
                if (p.x !in 0f..w.toFloat() || p.y !in 0f..h.toFloat()) {
                    p.x = Random.nextInt(w).toFloat()
                    p.y = Random.nextInt(h).toFloat()
                }
            }
        }

        private fun drawWaves(canvas: Canvas) {
            val w = canvas.width
            val h = canvas.height
            val time = System.currentTimeMillis() / 100f

            canvas.drawColor(Color.BLACK)
            paint.color = colorFromName(colorName)
            paint.strokeWidth = 3f

            val centerY = h / 2f
            for (x in 0 until w step 5) {
                val y = (sin((x + time) * 0.05) * 50 + centerY).toFloat()
                canvas.drawCircle(x.toFloat(), y, 5f, paint)
            }
        }

        private fun colorFromName(name: String): Int {
            return when (name.lowercase()) {
                "أحمر", "red" -> Color.RED
                "أخضر", "green" -> Color.GREEN
                "أصفر", "yellow" -> Color.YELLOW
                "بنفسجي", "purple" -> Color.MAGENTA
                "سماوي", "cyan" -> Color.CYAN
                "أزرق", "blue" -> Color.BLUE
                "عشوائي", "random" -> Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                else -> Color.BLUE
            }
        }

        // ✅ مكان الكلاس الصحيح هنا
        private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float) {
            companion object {
                fun random(w: Int, h: Int, size: Int): Particle {
                    val rnd = Random
                    val x = rnd.nextInt(w).toFloat()
                    val y = rnd.nextInt(h).toFloat()
                    val vx = (rnd.nextFloat() - 0.5f) * 4f
                    val vy = (rnd.nextFloat() - 0.5f) * 4f
                    return Particle(x, y, vx, vy)
                }
            }
        }
    }
}
