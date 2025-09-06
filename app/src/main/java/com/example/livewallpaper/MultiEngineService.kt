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

    inner class MultiEngine(private val context: Context) : Engine(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        private val prefs: SharedPreferences =
            context.getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var visible = true
        private var thread: Thread? = null

        // الإعدادات
        private var pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
        private var colorName = prefs.getString("color", "عشوائي") ?: "عشوائي"
        private var direction = prefs.getString("direction", "يمين") ?: "يمين"
        private var effect = prefs.getString("effect", "بدون") ?: "بدون"
        private var speed = prefs.getInt("speed", 5)
        private var size = prefs.getInt("size", 50)
        private var density = prefs.getInt("density", 5)

        // ألوان للتدرج
        private var currentColor = getColorFromName(colorName)
        private var targetColor = getRandomColor()
        private var colorLerp = 0f

        // جسيمات
        private var particles = mutableListOf<Particle>()

        init {
            prefs.registerOnSharedPreferenceChangeListener(this)
            initParticles()
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            stopDrawing()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) startDrawing() else stopDrawing()
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
                        Thread.sleep((40L - speed * 3).coerceAtLeast(5L))
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
                "جسيمات" -> drawParticles(canvas)
                "موجات" -> drawWaves(canvas)
            }
        }

        private fun drawGradient(canvas: Canvas) {
            val width = canvas.width.toFloat()
            val height = canvas.height.toFloat()

            if (colorLerp >= 1f) {
                currentColor = targetColor
                targetColor = pickTargetColor()
                colorLerp = 0f
            } else {
                colorLerp += 0.01f * (speed + 1)
            }

            val blended = blendColors(currentColor, targetColor, colorLerp)
            val shader = LinearGradient(
                0f, 0f, width, height,
                currentColor, blended,
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
            paint.style = Paint.Style.FILL
            for (p in particles) {
                paint.color = p.color
                canvas.drawCircle(p.x, p.y, p.r, paint)
                p.x += p.vx
                p.y += p.vy
                if (p.x < 0 || p.x > canvas.width || p.y < 0 || p.y > canvas.height) {
                    resetParticle(p, canvas.width, canvas.height)
                }
            }
        }

        private fun drawWaves(canvas: Canvas) {
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = getColorFromName(colorName)

            val amplitude = size.toFloat()
            val frequency = 2 * PI / 200
            val height = canvas.height / 2f
            val path = Path()
            path.moveTo(0f, height)
            for (x in 0..canvas.width step 10) {
                val y = (height + amplitude * sin(frequency * x + System.currentTimeMillis() / 300.0)).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            canvas.drawPath(path, paint)
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

        private fun blendColors(c1: Int, c2: Int, ratio: Float): Int {
            val inv = 1 - ratio
            val a = (Color.alpha(c1) * inv + Color.alpha(c2) * ratio).toInt()
            val r = (Color.red(c1) * inv + Color.red(c2) * ratio).toInt()
            val g = (Color.green(c1) * inv + Color.green(c2) * ratio).toInt()
            val b = (Color.blue(c1) * inv + Color.blue(c2) * ratio).toInt()
            return Color.argb(a, r, g, b)
        }

        private fun pickTargetColor(): Int {
            return if (colorName == "عشوائي") getRandomColor() else getColorFromName(colorName)
        }

        private fun initParticles() {
            particles.clear()
            val w = 1080
            val h = 1920
            repeat(density * 20) {
                val p = Particle(
                    Random.nextInt(w).toFloat(),
                    Random.nextInt(h).toFloat(),
                    Random.nextFloat() * 4f - 2f,
                    Random.nextFloat() * 4f - 2f,
                    size.toFloat(),
                    getRandomColor()
                )
                particles.add(p)
            }
        }

        private fun resetParticle(p: Particle, w: Int, h: Int) {
            p.x = Random.nextInt(w).toFloat()
            p.y = Random.nextInt(h).toFloat()
            p.vx = Random.nextFloat() * 4f - 2f
            p.vy = Random.nextFloat() * 4f - 2f
            p.r = size.toFloat()
            p.color = getRandomColor()
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                "pattern" -> pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
                "color" -> {
                    colorName = prefs.getString("color", "عشوائي") ?: "عشوائي"
                    currentColor = getColorFromName(colorName)
                    targetColor = pickTargetColor()
                    colorLerp = 0f
                    initParticles()
                }
                "direction" -> direction = prefs.getString("direction", "يمين") ?: "يمين"
                "effect" -> effect = prefs.getString("effect", "بدون") ?: "بدون"
                "speed" -> speed = prefs.getInt("speed", 5)
                "size" -> { size = prefs.getInt("size", 50); initParticles() }
                "density" -> { density = prefs.getInt("density", 5); initParticles() }
            }
        }

        // تعريفات الجسيمات
        private data class Particle(
            var x: Float, var y: Float,
            var vx: Float, var vy: Float,
            var r: Float, var color: Int
        )
    }
}
