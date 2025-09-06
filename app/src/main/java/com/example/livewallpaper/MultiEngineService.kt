package com.example.livewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
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

        // إعدادات من SharedPreferences
        private var pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
        private var color = prefs.getString("color", "عشوائي") ?: "عشوائي"
        private var speed = prefs.getInt("speed", 5)

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
                        // التحكم في سرعة التحديث
                        Thread.sleep((100 - speed * 9).toLong().coerceAtLeast(10))
                    } catch (_: InterruptedException) { }
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
