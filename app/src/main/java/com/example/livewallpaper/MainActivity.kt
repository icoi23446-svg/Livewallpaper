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
        val spinnerPattern: Spinner = findViewById(R.id.spinnerPattern)
        val spinnerColor: Spinner = findViewById(R.id.spinnerColor)
        val spinnerDirection: Spinner = findViewById(R.id.spinnerDirection)
        val spinnerEffect: Spinner = findViewById(R.id.spinnerEffect)

        val seekBarSpeed: SeekBar = findViewById(R.id.seekBarSpeed)
        val seekBarSize: SeekBar = findViewById(R.id.seekBarSize)
        val seekBarDensity: SeekBar = findViewById(R.id.seekBarDensity)

        val btnApply: Button = findViewById(R.id.btnApply)

        // استرجاع القيم القديمة
        spinnerPattern.setSelection(getIndex(spinnerPattern, prefs.getString("pattern", "Animated Gradient")!!))
        spinnerColor.setSelection(getIndex(spinnerColor, prefs.getString("color", "أزرق")!!))
        spinnerDirection.setSelection(getIndex(spinnerDirection, prefs.getString("direction", "يمين")!!))
        spinnerEffect.setSelection(getIndex(spinnerEffect, prefs.getString("effect", "بدون")!!))

        seekBarSpeed.progress = prefs.getInt("speed", 5)
        seekBarSize.progress = prefs.getInt("size", 50)
        seekBarDensity.progress = prefs.getInt("density", 5)

        // عند الضغط على زر "تطبيق"
        btnApply.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("pattern", spinnerPattern.selectedItem.toString())
            editor.putString("color", spinnerColor.selectedItem.toString())
            editor.putString("direction", spinnerDirection.selectedItem.toString())
            editor.putString("effect", spinnerEffect.selectedItem.toString())
            editor.putInt("speed", seekBarSpeed.progress)
            editor.putInt("size", seekBarSize.progress)
            editor.putInt("density", seekBarDensity.progress)
            editor.apply()

            // شغّل شاشة تعيين الخلفية الحية
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        android.content.ComponentName(this@MainActivity, MultiEngineService::class.java))
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "لم يتم دعم الخلفيات الحية على هذا الجهاز", Toast.LENGTH_LONG).show()
            }
        }
    }

    // دالة مساعدة لاسترجاع index العنصر
    private fun getIndex(spinner: Spinner, value: String): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
