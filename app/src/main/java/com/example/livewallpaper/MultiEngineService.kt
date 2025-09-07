package com.example.livewallpaper

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import android.util.Log

class MultiEngineService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return GradientEngine()
    }

    inner class GradientEngine : Engine() {
        private val paint = Paint()
        private var running = true

        private var pattern: String = "تدرج لوني"
        private var color: Int = Color.BLUE

        override fun onVisibilityChanged(visible: Boolean) {
            running = visible
            if (visible) {
                drawFrame()
            }
        }

        private fun drawFrame() {
            val holder: SurfaceHolder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    Log.d("Wallpaper", "Pattern = $pattern, Color = $color")

                    when (pattern) {
                        "تدرج لوني" -> drawGradient(canvas)
                        "تغير لون" -> drawColorShift(canvas)
                        else -> {
                            paint.color = Color.GRAY
                            canvas.drawRect(
                                0f, 0f, 
                                canvas.width.toFloat(), 
                                canvas.height.toFloat(), 
                                paint
                            )
                        }
                    }
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas)
                }
            }
        }

        private fun drawGradient(canvas: Canvas) {
            val shader = LinearGradient(
                0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(),
                intArrayOf(Color.RED, Color.BLUE, Color.GREEN),
                null,
                Shader.TileMode.CLAMP
            )
            paint.shader = shader
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
            paint.shader = null
        }

        private fun drawColorShift(canvas: Canvas) {
            paint.color = color
            canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
        }

        override fun onDestroy() {
            super.onDestroy()
            running = false
        }
    }
}
