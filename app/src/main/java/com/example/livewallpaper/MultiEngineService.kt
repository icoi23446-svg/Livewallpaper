package com.example.livewallpaper

import android.service.wallpaper.WallpaperService
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.view.SurfaceHolder
import kotlin.math.*

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return PatternEngine()
    }

    inner class PatternEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint()
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
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    // قراءة الإعدادات
                    val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
                    val color = prefs.getString("color", "أزرق") ?: "أزرق"
                    val pattern = prefs.getString("pattern", "موجات") ?: "موجات"
                    val direction = prefs.getString("direction", "يمين") ?: "يمين"
                    val effect = prefs.getString("effect", "بدون") ?: "بدون"
                    val speed = prefs.getInt("speed", 5)
                    val size = prefs.getInt("size", 50)
                    val density = prefs.getInt("density", 5)

                    // تحويل اللون
                    val colorInt = when (color) {
                        "أحمر" -> Color.RED
                        "أخضر" -> Color.GREEN
                        "أصفر" -> Color.YELLOW
                        "بنفسجي" -> Color.MAGENTA
                        "سماوي" -> Color.CYAN
                        "عشوائي" -> Color.rgb((0..255).random(), (0..255).random(), (0..255).random())
                        else -> Color.BLUE
                    }

                    // خلفية
                    canvas.drawColor(Color.BLACK)
                    paint.color = colorInt
                    paint.style = Paint.Style.FILL

                    val w = canvas.width
                    val h = canvas.height

                    // حركة الاتجاه
                    val dx = when (direction) {
                        "يمين" -> speed.toFloat()
                        "يسار" -> -speed.toFloat()
                        else -> 0f
                    }
                    val dy = when (direction) {
                        "أسفل" -> speed.toFloat()
                        "أعلى" -> -speed.toFloat()
                        else -> 0f
                    }
                    angle += 0.1f * speed

                    // اختيار النمط
                    when (pattern) {
                        "موجات" -> {
                            for (i in 0..h step max(20, 200 / density)) {
                                val y = i + (sin(angle) * size).toInt()
                                canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), paint)
                            }
                        }
                        "دوائر" -> {
                            for (i in 0..w step max(60, 300 / density)) {
                                for (j in 0..h step max(60, 300 / density)) {
                                    canvas.drawCircle(
                                        i + dx * angle, j + dy * angle,
                                        size / 2f + 10f * sin(angle),
                                        paint
                                    )
                                }
                            }
                        }
                        "مربعات" -> {
                            for (i in 0..w step max(60, 300 / density)) {
                                for (j in 0..h step max(60, 300 / density)) {
                                    canvas.drawRect(
                                        i.toFloat(), j.toFloat(),
                                        (i + size).toFloat(), (j + size).toFloat(), paint
                                    )
                                }
                            }
                        }
                        "نجوم" -> {
                            for (i in 0..w step max(60, 200 / density)) {
                                val y = (h/2 + sin((i + angle) / 20) * size).toFloat()
                                canvas.drawCircle(i.toFloat(), y, (size / 5).toFloat(), paint)
                            }
                        }
                        "نقاط" -> {
                            for (i in 0..w step max(30, 200 / density)) {
                                for (j in 0..h step max(30, 200 / density)) {
                                    canvas.drawCircle(i.toFloat(), j.toFloat(), (size/10).toFloat(), paint)
                                }
                            }
                        }
                        "تدرج لوني" -> {
                            val shader = LinearGradient(
                                0f, 0f, w.toFloat(), h.toFloat(),
                                intArrayOf(Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN),
                                null,
                                Shader.TileMode.MIRROR
                            )
                            paint.shader = shader
                            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
                            paint.shader = null
                        }
                        "عشوائي" -> {
                            val shapes = listOf("موجات","دوائر","مربعات","نجوم","نقاط")
                            val randomPattern = shapes.random()
                            // استدعاء النمط العشوائي
                        }
                    }

                    // التأثيرات
                    when (effect) {
                        "وميض" -> paint.alpha = (128 + 127 * sin(angle)).toInt()
                        "دوران" -> canvas.rotate(angle * 2, (w/2).toFloat(), (h/2).toFloat())
                        "شفافية" -> paint.alpha = 128
                        else -> paint.alpha = 255
                    }
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
        }
    }
}
