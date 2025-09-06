package com.example.livewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return MultiEngine(this)
    }

    inner class MultiEngine(private val context: Context) : Engine() {

        private val prefs: SharedPreferences =
            context.getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        private val paint = Paint()
        private var visible = true
        private var thread: Thread? = null

        // الإعدادات من SharedPreferences
        private var pattern: String = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
        private var color: String = prefs.getString("color", "عشوائي") ?: "عشوائي"
        private var direction: String = prefs.getString("direction", "يمين") ?: "يمين"
        private var effect: String = prefs.getString("effect", "بدون") ?: "بدون"
        private var speed: Int = prefs.getInt("speed", 5)
        private var size: Int = prefs.getInt("size", 50)
        private var density: Int = prefs.getInt("density", 5)

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) startDrawing() else stopDrawing()
        }

        override fun onDestroy() {
            super.onDestroy()
            stopDrawing()
        }

        private fun startDrawing() {
            thread = Thread {
                while (visible) {
                    val holder: SurfaceHolder = surfaceHolder
                    var canvas: Canvas? = null
                    try {
                        canvas = holder.lockCanvas()
                        if (canvas != null) {
                            drawFrame(canvas)
                        }
                    } finally {
                        if (canvas != null) holder.unlockCanvasAndPost(canvas)
                    }
                    try {
                        Thread.sleep(40L * (11 - speed)) // التحكم في السرعة (1 بطيء – 10 سريع)
                    } catch (_: InterruptedException) {
                    }
                }
            }
            thread?.start()
        }

        private fun stopDrawing() {
            visible = false
            thread?.interrupt()
            thread = null
        }

        private fun drawFrame(canvas: Canvas) {
            canvas.drawColor(Color.BLACK)

            when (pattern) {
                "تدرج لوني" -> drawGradient(canvas)
                "تغير لون" -> drawColorShift(canvas)
                "جسيمات" -> drawParticles(canvas)
                "موجات" -> drawWaves(canvas)
            }
        }

        private fun drawGradient(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()
            val shader = LinearGradient(
                0f, 0f, width, height,
                getColorFromName(color), getRandomColor(),
                Shader.TileMode.MIRROR
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, width, height, paint)
        }

        private fun drawColorShift(canvas: Canvas) {
            paint.shader = null
            paint.color = getRandomColor()
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }

        private fun drawParticles(canvas: Canvas) {
            paint.shader = null
            for (i in 0 until density * 10) {
                paint.color = getColorFromName(color)
                val x = Random.nextInt(canvas.width).toFloat()
                val y = Random.nextInt(canvas.height).toFloat()
                canvas.drawCircle(x, y, size.toFloat(), paint)
            }
        }

        private fun drawWaves(canvas: Canvas) {
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = getColorFromName(color)

            val amplitude = size.toFloat()
            val frequency = 2 * PI / 200
            val centerY = canvas.height / 2f

            val path = Path()
            path.moveTo(0f, centerY)
            for (x in 0..canvas.width step 10) {
                val y = (centerY + amplitude * sin(frequency * x + System.currentTimeMillis() / 500.0)).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            canvas.drawPath(path, paint)
            paint.style = Paint.Style.FILL
        }

        private fun getColorFromName(name: String): Int {
            return when (name) {
                "أحمر" -> Color.RED
                "أزرق" -> Color.BLUE
                "أخضر" -> Color.GREEN
                "أصفر" -> Color.YELLOW
                "بنفسجي" -> Color.MAGENTA
                "سماوي" -> Color.CYAN
                else -> getRandomColor()
            }
        }

        private fun getRandomColor(): Int {
            val rnd = Random.Default
            return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
        }
    }
}
