package com.example.livewallpaper

import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import kotlin.math.sin

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return GradientEngine()
    }

    inner class GradientEngine : Engine() {
        private val paint = Paint()
        private val handler = Handler(Looper.getMainLooper())
        private var running = true
        private var time = 0.0

        private val drawRunner = object : Runnable {
            override fun run() {
                drawFrame()
                if (running) {
                    handler.postDelayed(this, 50) // كل 50ms يرسم فريم جديد
                }
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

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    time += 0.1

                    // ألوان بتتغير مع الزمن
                    val r = (128 + 127 * sin(time)).toInt()
                    val g = (128 + 127 * sin(time + 2)).toInt()
                    val b = (128 + 127 * sin(time + 4)).toInt()

                    val shader = LinearGradient(
                        0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
                        intArrayOf(Color.rgb(r, g, b), Color.rgb(g, b, r)),
                        null,
                        Shader.TileMode.CLAMP
                    )
                    paint.shader = shader
                    canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
                    paint.shader = null
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            running = false
            handler.removeCallbacks(drawRunner)
        }
    }
}
