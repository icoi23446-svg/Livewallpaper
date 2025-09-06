package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)

        // اربط العناصر من الواجهة
        val patternSpinner = findViewById<Spinner>(R.id.patternSpinner)
        val colorSpinner = findViewById<Spinner>(R.id.colorSpinner)
        val directionSpinner = findViewById<Spinner>(R.id.directionSpinner)
        val effectSpinner = findViewById<Spinner>(R.id.effectSpinner)
        val speedSeekBar = findViewById<SeekBar>(R.id.speedSeekBar)
        val sizeSeekBar = findViewById<SeekBar>(R.id.sizeSeekBar)
        val densitySeekBar = findViewById<SeekBar>(R.id.densitySeekBar)
        val applyButton = findViewById<Button>(R.id.applyButton)

        // حمّل البيانات من strings.xml
        val patterns = resources.getStringArray(R.array.patterns_array)
        val colors = resources.getStringArray(R.array.colors_array)
        val directions = resources.getStringArray(R.array.direction_array)
        val effects = resources.getStringArray(R.array.effects_array)

        // اربط الـ Spinners بمحول (Adapter)
        patternSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, patterns)
        colorSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors)
        directionSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, directions)
        effectSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, effects)

        // استرجاع القيم المحفوظة
        patternSpinner.setSelection(patterns.indexOf(prefs.getString("pattern", patterns[0])))
        colorSpinner.setSelection(colors.indexOf(prefs.getString("color", colors[0])))
        directionSpinner.setSelection(directions.indexOf(prefs.getString("direction", directions[0])))
        effectSpinner.setSelection(effects.indexOf(prefs.getString("effect", effects[0])))
        speedSeekBar.progress = prefs.getInt("speed", 5)
        sizeSeekBar.progress = prefs.getInt("size", 50)
        densitySeekBar.progress = prefs.getInt("density", 5)

        // زر التطبيق
        applyButton.setOnClickListener {
            // خزّن القيم في SharedPreferences
            prefs.edit()
                .putString("pattern", patternSpinner.selectedItem.toString())
                .putString("color", colorSpinner.selectedItem.toString())
                .putString("direction", directionSpinner.selectedItem.toString())
                .putString("effect", effectSpinner.selectedItem.toString())
                .putInt("speed", speedSeekBar.progress)
                .putInt("size", sizeSeekBar.progress)
                .putInt("density", densitySeekBar.progress)
                .apply()

            // افتح شاشة اختيار الخلفية الحية
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    android.content.ComponentName(this, MultiEngineService::class.java)
                )
                startActivity(intent)
            } catch (e: Exception) {
                // fallback لو الجهاز ما يدعم ACTION_CHANGE_LIVE_WALLPAPER
                startActivity(Intent(Settings.ACTION_WALLPAPER_SETTINGS))
            }
        }
    }
}
