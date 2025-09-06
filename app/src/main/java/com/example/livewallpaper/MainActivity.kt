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
        val patternSpinner = findViewById<Spinner>(R.id.patternSpinner)
        val colorSpinner = findViewById<Spinner>(R.id.colorSpinner)
        val directionSpinner = findViewById<Spinner>(R.id.directionSpinner)
        val effectSpinner = findViewById<Spinner>(R.id.effectSpinner)
        val speedSeek = findViewById<SeekBar>(R.id.speedSeekBar)
        val sizeSeek = findViewById<SeekBar>(R.id.sizeSeekBar)
        val densitySeek = findViewById<SeekBar>(R.id.densitySeekBar)
        val applyButton = findViewById<Button>(R.id.applyButton)

        // تحميل القيم المخزنة مسبقاً
        patternSpinner.setSelection(getIndex(patternSpinner, prefs.getString("pattern", "تدرج لوني")))
        colorSpinner.setSelection(getIndex(colorSpinner, prefs.getString("color", "أزرق")))
        directionSpinner.setSelection(getIndex(directionSpinner, prefs.getString("direction", "يمين")))
        effectSpinner.setSelection(getIndex(effectSpinner, prefs.getString("effect", "بدون")))
        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)

        // زر تطبيق الخلفية
        applyButton.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("pattern", patternSpinner.selectedItem.toString())
            editor.putString("color", colorSpinner.selectedItem.toString())
            editor.putString("direction", directionSpinner.selectedItem.toString())
            editor.putString("effect", effectSpinner.selectedItem.toString())
            editor.putInt("speed", speedSeek.progress)
            editor.putInt("size", sizeSeek.progress)
            editor.putInt("density", densitySeek.progress)
            editor.apply()

            // فتح شاشة تعيين الخلفية الحية
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        android.content.ComponentName(
                            this@MainActivity,
                            MultiEngineService::class.java
                        )
                    )
                }
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "لم يتم دعم الخلفيات الحية على جهازك", Toast.LENGTH_LONG).show()
            }
        }
    }

    // دالة تساعد على اختيار العنصر الصحيح في الـ Spinner
    private fun getIndex(spinner: Spinner, value: String?): Int {
        if (value == null) return 0
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
