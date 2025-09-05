package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val patternSpinner = findViewById<MaterialAutoCompleteTextView>(R.id.patternSpinner)
        val speedSeekBar = findViewById<SeekBar>(R.id.speedSeekBar)
        val speedLabel = findViewById<TextView>(R.id.speedLabel)
        val applyButton = findViewById<MaterialButton>(R.id.applyButton)

        var speed = 5

        // تحديث السرعة
        speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                speed = progress
                speedLabel.text = "السرعة: $progress"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // زر التطبيق
        applyButton.setOnClickListener {
            try {
                val selectedPattern = patternSpinner.text.toString()

                // حفظ الإعدادات
                val prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)
                prefs.edit()
                    .putString("pattern", selectedPattern)
                    .putInt("speed", speed)
                    .apply()

                // فتح إعدادات تعيين الخلفية
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
