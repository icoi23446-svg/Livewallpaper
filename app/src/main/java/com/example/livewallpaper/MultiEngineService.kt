package com.example.livewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin
import kotlin.random.Random

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine = MultiEngine(this)

    inner class MultiEngine(private val ctx: Context) :
        Engine(), SharedPreferences.OnSharedPreferenceChangeListener {

        private val prefs: SharedPreferences =
            ctx.getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private var running = false
        private var drawThread: Thread? = null

        @Volatile private var pattern = prefs.getString("pattern", "تدرج لوني") ?: "تدرج لوني"
        @Volatile private var colorName = prefs.getString("color", "عشوائي") ?: "عشوائي"
        @Volatile private var direction = prefs.getString("direction", "يمين") ?: "يمين"
        @Volatile private var effect = prefs.getString("effect", "بدون") ?: "بدون"
        @Volatile private var speed = prefs.getInt("speed", 5)
        @Volatile private var size = prefs.getInt("size", 50)
        @Volatile private var density = prefs.getInt("density", 5)

        // زمن/سرعة
        private var t = 0.0
        private val baseStep get() = (speed.coerceIn(1, 10) / 10.0)         // 0.1..1.0
        private val frameDelayMs get() = (120 - speed.coerceIn(1,10)*10).coerceIn(16, 120)

        // جسيمات
        private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var r: Float, var color: Int)
        private var particles = mutableListOf<Particle>()

        // انتقال لوني سلس
        private var currentColor = randColor()
        private var targetColor = pickTargetColor()
        private var colorLerp = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            prefs.registerOnSharedPreferenceChangeListener(this)
            initParticles()
        }

        override fun onDestroy() {
            super.onDestroy()
            prefs.unregisterOnSharedPreferenceChangeListener(this)
            stop()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            if (visible) start() else stop()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            stop()
        }

        private fun start() {
            if (running) return
            running = true
            drawThread = Thread {
                while (running) {
                    var c: Canvas? = null
                    try {
                        c = surfaceHolder.lockCanvas()
                        if (c != null) drawFrame(c)
                    } catch (_: Throwable) {
                    } finally {
                        if (c != null) try { surfaceHolder.unlockCanvasAndPost(c) } catch (_: Throwable) {}
                    }

                    try { Thread.sleep(frameDelayMs.toLong()) } catch (_: InterruptedException) {}
                    t += baseStep * 0.1
                }
            }
            drawThread?.start()
        }

        private fun stop() {
            running = false
            drawThread?.interrupt()
            drawThread = null
        }

        private fun drawFrame(canvas: Canvas) {
            // خلفية داكنة خفيفة بدل السواد الكامل
            canvas.drawColor(Color.rgb(12,12,12))
            canvas.save()
            applyEffectPre(canvas)

            when (pattern) {
                "تدرج لوني" -> drawGradient(canvas)
                "تغير لون" -> drawColorShift(canvas)
                "جسيمات"   -> drawParticles(canvas)
                "موجات"    -> drawWaves(canvas)
                else       -> drawGradient(canvas)
            }

            canvas.restore()
        }

        private fun applyEffectPre(canvas: Canvas) {
            if (effect == "دوران") {
                val angle = (t * 30.0).toFloat()
                canvas.rotate(angle, canvas.width/2f, canvas.height/2f)
            }
        }

        private fun applyAlpha() {
            when (effect) {
                "بدون"   -> paint.alpha = 255
                "شفافية" -> paint.alpha = 140
                "وميض"   -> {
                    val a = (127.5 * (1 + kotlin.math.sin(t * 2 * Math.PI))).toInt()
                    paint.alpha = 60 + (a/2)
                }
                else -> paint.alpha = 255
            }
        }

        /** أنماط الرسم **/
        private fun drawGradient(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val c1 = resolveBaseColor()
            val c2 = lightenOrDarken(c1, 0.35f)

            val shift = ((kotlin.math.sin(t) + 1) / 2f)
            val (x0,y0,x1,y1) = when (direction) {
                "يمين" -> Quad(0f, 0f, w * shift, h)
                "يسار" -> Quad(w, 0f, w * (1 - shift), h)
                "أعلى" -> Quad(0f, h, w, h * (1 - shift))
                "أسفل" -> Quad(0f, 0f, w, h * shift)
                else   -> Quad(0f, 0f, w, h)
            }

            val shader = LinearGradient(x0, y0, x1, y1, c1, c2, Shader.TileMode.CLAMP)
            paint.shader = shader
            applyAlpha()
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
            paint.alpha = 255
        }

        private fun drawColorShift(canvas: Canvas) {
            colorLerp += (baseStep * 0.02f).toFloat() // بطيء وسلس
            if (colorLerp >= 1f) {
                currentColor = targetColor
                targetColor = pickTargetColor()
                colorLerp = 0f
            }
            val blended = lerpColor(currentColor, targetColor, colorLerp)
            paint.shader = null
            applyAlpha()
            paint.color = blended
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.alpha = 255
        }

        private fun drawParticles(canvas: Canvas) {
            ensureParticleCount()
            applyAlpha()
            paint.shader = null
            for (p in particles) {
                p.x += p.vx * speed
                p.y += p.vy * speed

                if (p.x < -p.r) p.x = canvas.width + p.r
                if (p.x > canvas.width + p.r) p.x = -p.r
                if (p.y < -p.r) p.y = canvas.height + p.r
                if (p.y > canvas.height + p.r) p.y = -p.r

                paint.color = p.color
                canvas.drawCircle(p.x, p.y, p.r, paint)
            }
            paint.alpha = 255
        }

        private fun drawWaves(canvas: Canvas) {
            applyAlpha()
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f + size * 0.05f
            paint.color = resolveBaseColor()

            val amp = 20f + size * 2f
            val freq = (2 * Math.PI / (150.0 + 5 * size)).toFloat()
            val mid = canvas.height/2f
            val phase = (t * 4).toFloat()

            val path = Path().apply { moveTo(0f, mid) }
            for (x in 0..canvas.width step 8) {
                val y = (mid + amp * kotlin.math.sin(freq * x + phase)).toFloat()
                path.lineTo(x.toFloat(), y)
            }
            canvas.drawPath(path, paint)

            // موجة ثانية خفيفة
            paint.color = lightenOrDarken(paint.color, 0.25f)
            val path2 = Path().apply { moveTo(0f, mid) }
            for (x in 0..canvas.width step 8) {
                val y = (mid + (amp*0.7f) * kotlin.math.sin(freq * x + phase + 1.2f)).toFloat()
                path2.lineTo(x.toFloat(), y)
            }
            canvas.drawPath(path2, paint)

            paint.style = Paint.Style.FILL
            paint.alpha = 255
        }

        /** تهيئة/مساعدة **/
        private fun initParticles() {
            particles.clear()
            val n = (density.coerceIn(1,10) * 25) // 25..250
            val baseR = 2f + size * 0.2f
            val (dx,dy) = dirVector()
            repeat(n) {
                val r = baseR * (0.5f + Random.nextFloat())
                val speedUnit = 0.2f + Random.nextFloat() * 0.8f
                particles += Particle(
                    x = Random.nextFloat()* 1080f,
                    y = Random.nextFloat()* 1920f,
                    vx = dx * speedUnit,
                    vy = dy * speedUnit,
                    r = r,
                    color = if (colorName == "عشوائي") randColor() else resolveBaseColor()
                )
            }
        }

        private fun ensureParticleCount() {
            val wanted = (density.coerceIn(1,10) * 25)
            if (particles.size != wanted) initParticles()
        }

        private fun dirVector(): Pair<Float, Float> = when(direction) {
            "يمين" -> 1f to 0f
            "يسار" -> -1f to 0f
            "أعلى" -> 0f to -1f
            "أسفل" -> 0f to 1f
            else   -> 1f to 0f
        }

        private fun resolveBaseColor(): Int = when (colorName) {
            "أحمر"   -> Color.rgb(220, 20, 60)
            "أزرق"   -> Color.rgb(33, 150, 243)
            "أخضر"   -> Color.rgb(76, 175, 80)
            "أصفر"   -> Color.rgb(255, 235, 59)
            "بنفسجي" -> Color.rgb(156, 39, 176)
            "سماوي"  -> Color.rgb(0, 188, 212)
            "عشوائي" -> randColor()
            else     -> Color.rgb(120,120,120)
        }

        private fun randColor(): Int =
            Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

        private fun lightenOrDarken(color: Int, amount: Float): Int {
            val r = Color.red(color); val g = Color.green(color); val b = Color.blue(color)
            val factor = (1 + amount).coerceAtLeast(0f)
            val nr = (r * factor).coerceIn(0f, 255f).toInt()
            val ng = (g * factor).coerceIn(0f, 255f).toInt()
            val nb = (b * factor).coerceIn(0f, 255f).toInt()
            return Color.rgb(nr, ng, nb)
        }

        private fun lerpColor(a: Int, b: Int, t: Float): Int {
            val tt = t.coerceIn(0f,1f)
            val ar = Color.red(a); val ag = Color.green(a); val ab = Color.blue(a)
            val br = Color.red(b); val bg = Color.green(b); val bb = Color.blue(b)
            val rr = (ar + (br - ar) * tt).toInt()
            val rg = (ag + (bg - ag) * tt).toInt()
            val rb = (ab + (bb - ab) * tt).toInt()
            return Color.rgb(rr, rg, rb)
        }

        private fun pickTargetColor(): Int =
            if (colorName == "عشوائي") randColor() else lightenOrDarken(resolveBaseColor(), 0.5f)

        /** تحديث حي عند تغيير الإعدادات من الواجهة **/
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            when (key) {
                "pattern" -> pattern = prefs.getString("pattern","تدرج لوني") ?: "تدرج لوني"
                "color" -> {
                    colorName = prefs.getString("color","عشوائي") ?: "عشوائي"
                    currentColor = resolveBaseColor()
                    targetColor = pickTargetColor()
                    colorLerp = 0f
                    initParticles()
                }
                "direction" -> { direction = prefs.getString("direction","يمين") ?: "يمين"; initParticles() }
                "effect" -> effect = prefs.getString("effect","بدون") ?: "بدون"
                "speed" -> speed = prefs.getInt("speed",5)
                "size" -> { size = prefs.getInt("size",50); initParticles() }
                "density" -> { density = prefs.getInt("density",5); initParticles() }
            }
        }

        /** مساعد صغير لتعريف رباعية قيم */
        private data class Quad(val x0: Float, val y0: Float, val x1: Float, val y1: Float)
    }
}
