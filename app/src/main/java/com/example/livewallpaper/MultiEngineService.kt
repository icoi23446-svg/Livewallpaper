package com.example.livewallpaper

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class MultiEngineService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return EngineImpl()
    }

    inner class EngineImpl : Engine() {
        private var running = true
        private var drawThread: Thread? = null
        private var width = 0
        private var height = 0

        private var t = 0f

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            running = true
        }

        override fun onDestroy() {
            running = false
            drawThread?.interrupt()
            super.onDestroy()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) startDrawing()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            this.width = width
            this.height = height
            startDrawing()
        }

        private fun startDrawing() {
            if (drawThread?.isAlive == true) return
            drawThread = thread(start = true) {
                val holder = surfaceHolder
                val rnd = Random
                // load prefs
                val prefs = getSharedPreferences("live_prefs", MODE_PRIVATE)
                var pattern = prefs.getInt("pattern", 0)
                var color1 = Color.parseColor(prefs.getString("color1", "#FF6B6B"))
                var color2 = Color.parseColor(prefs.getString("color2", "#5F27CD"))
                var speed = prefs.getInt("speed", 80).coerceAtLeast(1)

                val particles = MutableList(80) {
                    Particle(
                        rnd.nextFloat() * width,
                        rnd.nextFloat() * height,
                        (rnd.nextFloat() * 2 + 0.5f),
                        rnd.nextFloat() * 360
                    )
                }

                while (running) {
                    val nowPattern = prefs.getInt("pattern", pattern)
                    val nowColor1 = try { Color.parseColor(prefs.getString("color1", "#FF6B6B")) } catch (e: Exception){ color1 }
                    val nowColor2 = try { Color.parseColor(prefs.getString("color2", "#5F27CD")) } catch (e: Exception){ color2 }
                    val nowSpeed = prefs.getInt("speed", speed).coerceAtLeast(1)

                    var canvas: Canvas? = null
                    try {
                        canvas = holder.lockCanvas()
                        if (canvas == null) { Thread.sleep(16); continue }
                        val w = canvas.width.toFloat()
                        val h = canvas.height.toFloat()
                        canvas.drawColor(Color.BLACK)

                        t += nowSpeed / 200f

                        when (nowPattern) {
                            0 -> drawGradientShift(canvas, nowColor1, nowColor2, t)
                            1 -> drawAurora(canvas, nowColor1, nowColor2, t)
                            2 -> drawBlobs(canvas, nowColor1, nowColor2, t)
                            3 -> drawParticles(canvas, particles, nowColor1, nowColor2, t)
                            4 -> drawConicSpin(canvas, nowColor1, nowColor2, t)
                            else -> drawGradientShift(canvas, nowColor1, nowColor2, t)
                        }
                    } finally {
                        if (canvas != null) holder.unlockCanvasAndPost(canvas)
                    }
                    try { Thread.sleep(16) } catch (_: InterruptedException) { break }
                }
            }
        }

        // === Patterns ===

        private fun drawGradientShift(canvas: Canvas, c1: Int, c2: Int, t: Float) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val shader = LinearGradient(
                0f, 0f, 
                w * cos(t/5f), h * sin(t/5f),
                intArrayOf(c1, c2), null, Shader.TileMode.MIRROR
            )
            val p = Paint()
            p.shader = shader
            canvas.drawRect(0f, 0f, w, h, p)
        }

        private fun drawAurora(canvas: Canvas, c1: Int, c2: Int, t: Float) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val base = LinearGradient(
                0f, 0f, w, h, 
                blend(c1,0x88000000.toInt()), blend(c2,0x88000000.toInt()), 
                Shader.TileMode.CLAMP
            )
            val p = Paint()
            p.shader = base
            canvas.drawRect(0f, 0f, w, h, p)

            for (i in 0..4) {
                val gx = (w * (0.2f + 0.6f * (i/5f)) + 200f * sin(t/2f + i))
                val gy = (h * 0.3f + 150f * cos(t/3f + i))
                val radius = (w*0.6f/ (i+2))
                val rad = RadialGradient(
                    gx.toFloat(), gy.toFloat(), radius.toFloat(),
                    intArrayOf(blend(c1,0xAA000000.toInt()), blend(c2,0x00FFFFFF)), 
                    null, Shader.TileMode.CLAMP
                )
                val p2 = Paint(Paint.ANTI_ALIAS_FLAG)
                p2.shader = rad
                p2.alpha = 200/(i+1)
                canvas.drawCircle(gx.toFloat(), gy.toFloat(), radius.toFloat(), p2)
            }
        }

        private fun drawBlobs(canvas: Canvas, c1: Int, c2: Int, t: Float) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            val n = 8
            for (i in 0 until n) {
                val angle = t + i * (2*PI.toFloat()/n)
                val x = w/2 + (w*0.4f) * cos(angle + i)
                val y = h/2 + (h*0.25f) * sin(angle*1.1f + i)
                val r = w*0.15f * (0.5f + 0.5f * sin(t + i))
                val shader = RadialGradient(x, y, r, intArrayOf(c1, c2), null, Shader.TileMode.CLAMP)
                p.shader = shader
                p.alpha = 200
                canvas.drawCircle(x,y,r,p)
            }
        }

        private fun drawParticles(canvas: Canvas, particles: MutableList<Particle>, c1: Int, c2: Int, t: Float) {
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            for (prt in particles) {
                prt.x += cos(prt.angle) * prt.speed
                prt.y += sin(prt.angle) * prt.speed
                prt.angle += 0.01f
                if (prt.x < -50 || prt.x > w + 50 || prt.y < -50 || prt.y > h + 50) {
                    prt.x = Random.nextFloat()*w
                    prt.y = Random.nextFloat()*h
                }
                val shader = RadialGradient(prt.x, prt.y, 30f, intArrayOf(c1, c2), null, Shader.TileMode.CLAMP)
                p.shader = shader
                canvas.drawCircle(prt.x, prt.y, 30f, p)
            }
        }

        private fun drawConicSpin(canvas: Canvas, c1: Int, c2: Int, t: Float) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()
            val centerX = w/2
            val centerY = h/2
            val sweep = SweepGradient(centerX, centerY, intArrayOf(c1, c2, c1), null)
            val p = Paint(Paint.ANTI_ALIAS_FLAG)
            p.shader = sweep
            canvas.save()
            canvas.rotate((t*20f)%360, centerX, centerY)
            canvas.drawCircle(centerX, centerY, Math.min(w,h)/1.2f, p)
            canvas.restore()
        }

        private fun blend(color:Int, mask:Int): Int {
            val a = (mask ushr 24) and 0xff
            if (a==0) return color
            val r = (((color shr 16) and 0xff) * (255-a) + ((mask shr 16) and 0xff)*a)/255
            val g = (((color shr 8) and 0xff) * (255-a) + ((mask shr 8) and 0xff)*a)/255
            val b = (((color) and 0xff) * (255-a) + ((mask) and 0xff)*a)/255
            return (0xff shl 24) or (r shl 16) or (g shl 8) or b
        }

        data class Particle(var x: Float, var y: Float, var speed: Float, var angle: Float)
    }
}
