package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // عناصر الواجهة
        val colorSpinner = findViewById<Spinner>(R.id.colorSpinner)
        val patternSpinner = findViewById<Spinner>(R.id.patternSpinner)
        val directionSpinner = findViewById<Spinner>(R.id.directionSpinner)
        val effectsSpinner = findViewById<Spinner>(R.id.effectsSpinner)

        val speedSeekBar = findViewById<SeekBar>(R.id.speedSeekBar)
        val speedLabel = findViewById<TextView>(R.id.speedLabel)

        val sizeSeekBar = findViewById<SeekBar>(R.id.sizeSeekBar)
        val sizeLabel = findViewById<TextView>(R.id.sizeLabel)

        val densitySeekBar = findViewById<SeekBar>(R.id.densitySeekBar)
        val densityLabel = findViewById<TextView>(R.id.densityLabel)

        val applyButton = findViewById<Button>(R.id.applyButton)

        // متغيرات
        var speed = 5
        var size = 50
        var density = 5

        // تحديث السرعة
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speed = progress
                speedLabel.text = "السرعة: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // تحديث الحجم
        sizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                size = progress
                sizeLabel.text = "الحجم: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // تحديث الكثافة
        densitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                density = progress
                densityLabel.text = "الكثافة: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // زر التطبيق
        applyButton.setOnClickListener {
            try {
                val selectedColor = colorSpinner.selectedItem.toString()
                val selectedPattern = patternSpinner.selectedItem.toString()
                val selectedDirection = directionSpinner.selectedItem.toString()
                val selectedEffect = effectsSpinner.selectedItem.toString()

                // حفظ الإعدادات في SharedPreferences
                val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
                prefs.edit()
                    .putString("color", selectedColor)
                    .putString("pattern", selectedPattern)
                    .putString("direction", selectedDirection)
                    .putString("effect", selectedEffect)
                    .putInt("speed", speed)
                    .putInt("size", size)
                    .putInt("density", density)
                    .apply()

                // فتح شاشة تعيين الخلفية
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, MultiEngineService::class.java)
                    )
                }
                startActivity(intent)

            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
