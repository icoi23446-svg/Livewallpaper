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

        val colorSpinner = findViewById<Spinner>(R.id.colorSpinner)
        val patternSpinner = findViewById<Spinner>(R.id.patternSpinner)
        val speedSeekBar = findViewById<SeekBar>(R.id.speedSeekBar)
        val speedLabel = findViewById<TextView>(R.id.speedLabel)
        val applyButton = findViewById<Button>(R.id.applyButton)

        var speed = 5

        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speed = progress
                speedLabel.text = "السرعة: $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        applyButton.setOnClickListener {
            try {
                val selectedColor = colorSpinner.selectedItem.toString()
                val selectedPattern = patternSpinner.selectedItem.toString()

                // هنا نحفظ الإعدادات في SharedPreferences عشان خدمة الخلفية تستخدمها
                val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
                prefs.edit()
                    .putString("color", selectedColor)
                    .putString("pattern", selectedPattern)
                    .putInt("speed", speed)
                    .apply()

                // فتح شاشة اختيار الخلفية
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
