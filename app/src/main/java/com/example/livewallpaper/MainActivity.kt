package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)

        // عناصر الواجهة
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val speedSeek: SeekBar = findViewById(R.id.speedSeekBar)
        val sizeSeek: SeekBar = findViewById(R.id.sizeSeekBar)
        val densitySeek: SeekBar = findViewById(R.id.densitySeekBar)
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)
        val applyBtn: Button = findViewById(R.id.applyButton)

        // تحميل القيم القديمة
        loadSettings(patternSpinner, colorSpinner, speedSeek, sizeSeek, densitySeek, directionSpinner, effectSpinner)

        // زر التطبيق
        applyBtn.setOnClickListener {
            saveSettings(
                patternSpinner.selectedItem.toString(),
                colorSpinner.selectedItem.toString(),
                speedSeek.progress,
                sizeSeek.progress,
                densitySeek.progress,
                directionSpinner.selectedItem.toString(),
                effectSpinner.selectedItem.toString()
            )

            // تشغيل شاشة تعيين الخلفية
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    android.content.ComponentName(
                        this@MainActivity,
                        MultiEngineService::class.java
                    )
                )
            }
            startActivity(intent)
        }
    }

    private fun saveSettings(
        pattern: String,
        color: String,
        speed: Int,
        size: Int,
        density: Int,
        direction: String,
        effect: String
    ) {
        prefs.edit().apply {
            putString("pattern", pattern)
            putString("color", color)
            putInt("speed", speed)
            putInt("size", size)
            putInt("density", density)
            putString("direction", direction)
            putString("effect", effect)
            apply()
        }
    }

    private fun loadSettings(
        patternSpinner: Spinner,
        colorSpinner: Spinner,
        speedSeek: SeekBar,
        sizeSeek: SeekBar,
        densitySeek: SeekBar,
        directionSpinner: Spinner,
        effectSpinner: Spinner
    ) {
        val pattern = prefs.getString("pattern", "تدرج لوني")
        val color = prefs.getString("color", "أزرق")
        val speed = prefs.getInt("speed", 5)
        val size = prefs.getInt("size", 50)
        val density = prefs.getInt("density", 5)
        val direction = prefs.getString("direction", "يمين")
        val effect = prefs.getString("effect", "بدون")

        setSpinnerSelection(patternSpinner, pattern)
        setSpinnerSelection(colorSpinner, color)
        setSpinnerSelection(directionSpinner, direction)
        setSpinnerSelection(effectSpinner, effect)

        speedSeek.progress = speed
        sizeSeek.progress = size
        densitySeek.progress = density
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value == null) return
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }
}
