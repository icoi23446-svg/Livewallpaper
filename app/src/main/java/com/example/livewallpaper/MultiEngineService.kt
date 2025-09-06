package com.example.livewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import kotlin.random.Random

class MultiEngineService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return MultiEngine(this)
    }

    class MultiEngine(private val context: Context) : Engine() {

        private val prefs: SharedPreferences =
            context.getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        private val paint = Paint()
        private var running = true
        private var thread: Thread? = null

        // إعدادات
        private var pattern: String = "Animated Gradient"
        private var color: String = "Random"
        private var speed: Int = 5
        private var size: Int = 50
        private var density: Int = 5
        private var direction: String = "Right"
        private var effect: String = "None"

        // بيانات للرسم
        private var gradientShift = 0f
        private val particles = mutableListOf<Particle>()
        private var waveOffset = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            loadSettings()
            startDrawing()
        }

        override fun onDestroy() {
            super.onDestroy()
            stopDrawing()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) startDrawing() else stopDrawing()
        }

        // تحميل الإعدادات
        private fun loadSettings() {
            pattern = prefs.getString("pattern", "Animated Gradient") ?: "Animated Gradient"
            color = prefs.getString("color", "Random") ?: "Random"
            speed = prefs.getInt("speed", 5)
            size = prefs.getInt("size", 50)
            density = prefs.getInt("density", 5)
            direction = prefs.getString("direction", "Right") ?: "Right"
            effect = prefs.getString("effect", "None") ?: "None"
        }

        // تشغيل الرسم
        private fun startDrawing() {
            running = true
            thread = Thread {
                while (running) {
                    val holder = surfaceHolder
                    val canvas = holder.lockCanvas()
                    if (canvas != null) {
                        drawFrame(canvas)
                        holder.unlockCanvasAndPost(canvas)
                    }
                    try {
                        Thread.sleep((40L - speed).coerceAtLeast(5L))
                    } catch (_: InterruptedException) { }
                }
            }
            thread?.start()
        }

        private fun stopDrawing() {
            running = false
            thread?.interrupt()
        }

        // رسم الخلفية
        private fun drawFrame(canvas: Canvas) {
            canvas.drawColor(Color.BLACK)

            when (pattern) {
                "Animated Gradient", "تدرج لوني" -> drawGradient(canvas)
                "Color Cycle", "تغير لون" -> drawColorCycle(canvas)
                "Particles", "جسيمات" -> drawParticles(canvas)
                "Waves", "موجات" -> drawWaves(canvas)
            }
        }

        // 🎨 1. التدرج اللوني الناعم
        private fun drawGradient(canvas: Canvas) {
            gradientShift += 0.01f * speed
            val colors = intArrayOf(
                Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.CYAN
            )
            val shader = LinearGradient(
                0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
                colors, null, Shader.TileMode.MIRROR
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }

        // 🎨 2. تغيير الألوان
        private fun drawColorCycle(canvas: Canvas) {
            val baseColor = when (color) {
                "أحمر", "Red" -> Color.RED
                "أزرق", "Blue" -> Color.BLUE
                "أخضر", "Green" -> Color.GREEN
                "أصفر", "Yellow" -> Color.YELLOW
                "بنفسجي", "Purple" -> Color.MAGENTA
                "سماوي", "Cyan" -> Color.CYAN
                else -> Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
            }
            paint.color = baseColor
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }

        // 🎨 3. الجسيمات
        private fun drawParticles(canvas: Canvas) {
            if (particles.size < density * 10) {
                particles.add(Particle(canvas.width, canvas.height))
            }

            val iter = particles.iterator()
            while (iter.hasNext()) {
                val p = iter.next()
                p.update(speed)
                paint.color = p.color
                canvas.drawCircle(p.x, p.y, size / 5f, paint)
                if (p.isOffScreen(canvas.width, canvas.height)) {
                    iter.remove()
                }
            }
        }

        // 🎨 4. الموجات
        private fun drawWaves(canvas: Canvas) {
            waveOffset += 0.1f * speed
            paint.color = Color.CYAN
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f

            val centerY = canvas.height / 2f
            val amplitude = size.toFloat()
            val wavelength = 100f

            val path = Path()
            path.moveTo(0f, centerY)

            for (x in 0 until canvas.width step 10) {
                val y = centerY + amplitude * sin((x + waveOffset).toDouble() / wavelength * 2 * PI).toFloat()
                path.lineTo(x.toFloat(), y)
            }

            canvas.drawPath(path, paint)
        }
    }

    // ✅ الكلاس Particle اتنقل برة أي function
    class Particle(private val width: Int, private val height: Int) {
        var x = Random.nextFloat() * width
        var y = Random.nextFloat() * height
        private val dx = Random.nextFloat() * 4 - 2
        private val dy = Random.nextFloat() * 4 - 2
        val color: Int = Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

        fun update(speed: Int) {
            x += dx * (speed / 5f)
            y += dy * (speed / 5f)
        }

        fun isOffScreen(maxW: Int, maxH: Int): Boolean {
            return (x < 0 || y < 0 || x > maxW || y > maxH)
        }
    }
}
