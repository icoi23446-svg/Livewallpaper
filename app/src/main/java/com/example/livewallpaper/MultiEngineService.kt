package com.example.livewallpaper

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.*
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine = FancyEngine()

    inner class FancyEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var running = false
        private var angle = 0f
        private var hueShift = 0f
        private var particles: MutableList<Particle> = mutableListOf()
        private var lastPattern = ""
        private var lastDensity = -1
        private var lastSize = -1

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) handler.post(drawRunner)
            else handler.removeCallbacks(drawRunner)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            running = false
            handler.removeCallbacks(drawRunner)
        }

        override fun onDestroy() {
            super.onDestroy()
            handler.removeCallbacks(drawRunner)
        }

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas == null) {
                    scheduleNext(50)
                    return
                }

                // اقرأ الإعدادات
                val prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)
                val pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
                val colorName = prefs.getString("color", "أزرق") ?: "أزرق"
                val effect = prefs.getString("effect", "بدون") ?: "بدون"
                val direction = prefs.getString("direction", "يمين") ?: "يمين"
                val speedProgress = prefs.getInt("speed", 5)
                val sizeProgress = prefs.getInt("size", 50)
                val densityProgress = prefs.getInt("density", 5)

                val w = canvas.width
                val h = canvas.height
                val cx = w / 2f
                val cy = h / 2f

                // حساب معدل التحديث حسب السرعة
                val fps = (8 + (speedProgress.coerceIn(0, 10) * 5))
                val frameDelay = max(16L, (1000L / fps))

                // اللون الأساسي
                val baseColor = colorFromName(colorName)

                // تحديث الحركة
                angle += 0.02f * (speedProgress + 1)
                hueShift = (hueShift + 0.3f * (speedProgress + 1)) % 360f

                // إعادة تهيئة الجسيمات إذا تغيرت الإعدادات
                if (pattern != lastPattern || densityProgress != lastDensity || sizeProgress != lastSize) {
                    initParticlesIfNeeded(pattern, densityProgress, sizeProgress, w, h)
                    lastPattern = pattern
                    lastDensity = densityProgress
                    lastSize = sizeProgress
                }

                // تطبيق التأثير دوران
                val doRotate = effect == "دوران"
                if (doRotate) {
                    canvas.save()
                    canvas.rotate((angle * 10f) % 360f, cx, cy)
                }

                // أنماط الرسم
                when (pattern) {
                    "تدرج لوني", "Animated Gradient" -> drawAnimatedGradient(canvas, w, h, hueShift)
                    "تغير لون", "Color Cycle" -> drawColorCycle(canvas, angle)
                    "جسيمات", "Particles" -> drawParticles(canvas, w, h, baseColor, sizeProgress, densityProgress, direction, effect, angle)
                    "موجات", "Waves" -> drawWaves(canvas, w, h, baseColor, sizeProgress, densityProgress, direction, effect, angle)
                    else -> drawAnimatedGradient(canvas, w, h, hueShift)
                }

                if (doRotate) canvas.restore()

                scheduleNext(frameDelay)

            } finally {
                if (canvas != null) try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
            }
        }

        private fun scheduleNext(delayMs: Long) {
            if (running) {
                handler.removeCallbacks(drawRunner)
                handler.postDelayed(drawRunner, delayMs)
            }
        }

        // --------- أنماط ---------
        private fun drawAnimatedGradient(canvas: Canvas, w: Int, h: Int, hueShift: Float) {
            val colors = IntArray(5) { i ->
                val hue = (hueShift + i * 72f) % 360f
                Color.HSVToColor(floatArrayOf(hue, 0.9f, 1f))
            }
            val shader = LinearGradient(0f, 0f, w.toFloat(), h.toFloat(), colors, null, Shader.TileMode.MIRROR)
            paint.shader = shader
            canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
            paint.shader = null
        }

        private fun drawColorCycle(canvas: Canvas, angle: Float) {
            val hue = (angle * 20f) % 360f
            canvas.drawColor(Color.HSVToColor(floatArrayOf(hue, 0.85f, 1f)))
        }

        private fun drawParticles(
            canvas: Canvas, w: Int, h: Int,
            baseColor: Int, size: Int, density: Int,
            direction: String, effect: String, angle: Float
        ) {
            canvas.drawColor(Color.BLACK)
            paint.style = Paint.Style.FILL
            val baseRadius = (4f + size / 10f).coerceAtLeast(1f)

            val dxDir = when (direction) {
                "يمين" -> 1f
                "يسار" -> -1f
                else -> 0f
            }
            val dyDir = when (direction) {
                "أسفل" -> 1f
                "أعلى" -> -1f
                else -> 0f
            }

            for ((i, p) in particles.withIndex()) {
                p.x += p.vx + dxDir * (0.2f * (density + 1))
                p.y += p.vy + dyDir * (0.2f * (density + 1))

                if (p.x < -50) p.x = w + 50f
                if (p.x > w + 50) p.x = -50f
                if (p.y < -50) p.y = h + 50f
                if (p.y > h + 50) p.y = -50f

                val hue = (hueShift + i * 3) % 360
                val particleColor = if (baseColor != Color.TRANSPARENT) baseColor else Color.HSVToColor(floatArrayOf(hue, 0.8f, 1f))
                paint.color = particleColor

                paint.alpha = when (effect) {
                    "وميض" -> ((128 + 127 * sin(angle + i)).roundToInt()).coerceIn(30, 255)
                    "شفافية" -> 90
                    else -> 255
                }

                val radius = baseRadius + (if (effect == "وميض") abs(sin(angle + i)) * baseRadius else 0f)
                canvas.drawCircle(p.x, p.y, radius, paint)
            }
        }

        private fun drawWaves(
            canvas: Canvas, w: Int, h: Int,
            baseColor: Int, size: Int, density: Int,
            direction: String, effect: String, angle: Float
        ) {
            canvas.drawColor(Color.BLACK)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = max(1f, size / 8f)
            paint.color = baseColor
            paint.alpha = if (effect == "شفافية") 130 else 255

            val waves = (1 + density).coerceAtMost(8)
            val amplitude = 30f + size.toFloat()
            val step = 10

            for (wIndex in 0 until waves) {
                val phase = angle + wIndex * 0.6f
                val path = Path()
                var first = true
                for (x in 0 until w step step) {
                    val fx = x.toFloat()
                    val y = (h / 2f + sin((x * 0.02f) + phase) * (amplitude + wIndex * 8)).toFloat()
                    if (first) { path.moveTo(fx, y); first = false } else path.lineTo(fx, y)
                }
                canvas.drawPath(path, paint)
            }
        }

        private fun initParticlesIfNeeded(pattern: String, density: Int, size: Int, w: Int, h: Int) {
            if (pattern == "جسيمات" || pattern == "Particles") {
                val count = (10 + density * 30).coerceAtMost(800)
                if (particles.size != count || lastSize != size) {
                    particles = MutableList(count) { Particle.random(w, h, size) }
                }
            } else {
                particles.clear()
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
                "عشوائي", "random" -> Color.TRANSPARENT
                else -> Color.BLUE
            }
        }

        private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float) {
            companion object {
                fun random(w: Int, h: Int, size: Int): Particle {
                    val rnd = Random
                    val speedFactor = 0.3f + size / 80f
                    val vx = (rnd.nextFloat() - 0.5f) * 2f * speedFactor
                    val vy = (rnd.nextFloat() - 0.5f) * 2f * speedFactor
                    return Particle(
                        rnd.nextInt(max(1, w)).toFloat(),
                        rnd.nextInt(max(1, h)).toFloat(),
                        vx, vy
                    )
                }
            }
        }
    }
}
