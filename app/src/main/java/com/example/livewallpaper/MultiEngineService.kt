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
        private var lastPattern = ""
        private var particles: MutableList<Particle> = mutableListOf()
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

                val prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)
                val pattern = prefs.getString("pattern", "Animated Gradient") ?: "Animated Gradient"
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

                val fps = (8 + (speedProgress.coerceIn(0, 10) * 5))
                val frameDelay = max(16L, (1000L / fps))

                val baseColor = colorFromName(colorName)

                angle += 0.02f * (speedProgress.coerceIn(0, 10) + 1)
                hueShift = (hueShift + 0.3f * (speedProgress.coerceIn(0, 10) + 1)) % 360f

                if (pattern != lastPattern || densityProgress != lastDensity || sizeProgress != lastSize) {
                    initParticlesIfNeeded(pattern, densityProgress, sizeProgress, w, h)
                    lastPattern = pattern
                    lastDensity = densityProgress
                    lastSize = sizeProgress
                }

                val doRotate = effect == "دوران" || effect == "Rotate"
                if (doRotate) {
                    canvas.save()
                    val rotateDeg = (angle * 10f) % 360f
                    canvas.rotate(rotateDeg, cx, cy)
                }

                when (pattern) {
                    "Animated Gradient", "تدرج لوني", "Gradient" -> {
                        drawAnimatedGradient(canvas, w, h, hueShift)
                    }
                    "Color Cycle", "تغير لون", "Color Cycle" -> {
                        drawColorCycle(canvas, w, h, angle)
                    }
                    "Particles", "جسيمات", "Particles" -> {
                        drawParticles(canvas, w, h, baseColor, sizeProgress, densityProgress, direction, effect, angle)
                    }
                    "Waves", "موجات", "Waves" -> {
                        drawWaves(canvas, w, h, baseColor, sizeProgress, densityProgress, direction, effect, angle)
                    }
                    else -> {
                        drawAnimatedGradient(canvas, w, h, hueShift)
                    }
                }

                if (doRotate) canvas.restore()
                paint.shader = null
                scheduleNext(frameDelay)
            } finally {
                if (canvas != null) {
                    try { holder.unlockCanvasAndPost(canvas) } catch (_: Exception) {}
                }
            }
        }

        private fun scheduleNext(delayMs: Long) {
            if (running) {
                handler.removeCallbacks(drawRunner)
                handler.postDelayed(drawRunner, delayMs)
            }
        }

        // ---------- أنماط الرسم ----------
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

        private fun drawColorCycle(canvas: Canvas, w: Int, h: Int, angle: Float) {
            val hue = (angle * 20f) % 360f
            val color = Color.HSVToColor(floatArrayOf(hue, 0.85f, 1f))
            canvas.drawColor(color)
        }

        private fun drawParticles(
            canvas: Canvas,
            w: Int,
            h: Int,
            baseColor: Int,
            sizeProgress: Int,
            densityProgress: Int,
            direction: String,
            effect: String,
            angle: Float
        ) {
            canvas.drawColor(Color.BLACK)
            paint.style = Paint.Style.FILL
            val baseRadius = (4f + sizeProgress / 10f).coerceAtLeast(1f)

            val dxDir = when (direction) {
                "يمين", "Right" -> 1f
                "يسار", "Left" -> -1f
                else -> 0f
            }
            val dyDir = when (direction) {
                "أسفل", "Down" -> 1f
                "أعلى", "Up" -> -1f
                else -> 0f
            }

            for ((i, p) in particles.withIndex()) {
                p.x += p.vx + dxDir * (0.2f * (densityProgress + 1))
                p.y += p.vy + dyDir * (0.2f * (densityProgress + 1))
                p.x += sin((angle + i) * 0.02f) * (1 + sizeProgress / 40f)
                p.y += cos((angle + i) * 0.02f) * (1 + sizeProgress / 40f)

                if (p.x < -50) p.x = w + 50f
                if (p.x > w + 50) p.x = -50f
                if (p.y < -50) p.y = h + 50f
                if (p.y > h + 50) p.y = -50f

                val hue = (hueShift + i * 3) % 360
                val particleColor = if (baseColor != Color.TRANSPARENT) baseColor
                                    else Color.HSVToColor(floatArrayOf(hue, 0.8f, 1f))
                paint.color = particleColor

                val alpha = when (effect) {
                    "وميض", "Pulse" -> ((128 + 127 * sin(angle + i)).roundToInt()).coerceIn(30, 255)
                    "شفافية", "Transparency" -> 90
                    else -> 255
                }
                paint.alpha = alpha

                val radius = baseRadius + (if (effect == "وميض" || effect == "Pulse") abs(sin(angle + i)) * baseRadius else 0f)
                canvas.drawCircle(p.x, p.y, radius, paint)
            }
        }

        private fun drawWaves(
            canvas: Canvas,
            w: Int,
            h: Int,
            baseColor: Int,
            sizeProgress: Int,
            densityProgress: Int,
            direction: String,
            effect: String,
            angle: Float
        ) {
            canvas.drawColor(Color.BLACK)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = max(1f, sizeProgress / 8f)
            paint.color = baseColor
            paint.alpha = if (effect == "شفافية" || effect == "Transparency") 130 else 255

            val waves = (1 + densityProgress).coerceAtMost(8)
            val amplitude = 30f + sizeProgress.toFloat()
            val step = 10

            for (wIndex in 0 until waves) {
                val phase = angle + wIndex * 0.6f
                val path = Path()
                var first = true
                for (x in 0 until w step step) {
                    val fx = x.toFloat()
                    val y = (h / 2f + sin((x * 0.02f) + phase) * (amplitude + wIndex * 8)).toFloat()
                    if (first) {
                        path.moveTo(fx, y)
                        first = false
                    } else {
                        path.lineTo(fx, y)
                    }
                }
                canvas.drawPath(path, paint)
            }
        }

        private fun initParticlesIfNeeded(pattern: String, density: Int, size: Int, w: Int, h: Int) {
            if (pattern.startsWith("Particles", true) || pattern == "Particles" || pattern == "جسيمات") {
                val count = (10 + density * 30).coerceAtMost(800)
                if (particles.size != count || lastSize != size) {
                    particles = MutableList(count) { Particle.random(w, h, size) }
                }
            } else {
                if (particles.size > 0) particles.clear()
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

        // -------- كلاس Particle (برا أي دالة) --------
        private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float) {
            companion object {
                fun random(w: Int, h: Int, sizeProgress: Int): Particle {
                    val rnd = Random
                    val x = rnd.nextInt(max(1, w)).toFloat()
                    val y = rnd.nextInt(max(1, h)).toFloat()
                    val speedFactor = 0.3f + sizeProgress / 80f
                    val vx = (rnd.nextFloat() - 0.5f) * 2f * speedFactor
                    val vy = (rnd.nextFloat() - 0.5f) * 2f * speedFactor
                    return Particle(x, y, vx, vy)
                }
            }
        }
    }
}
