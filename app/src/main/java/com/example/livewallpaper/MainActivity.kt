package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var patternSpinner: Spinner
    private lateinit var colorSpinner: Spinner
    private lateinit var directionSpinner: Spinner
    private lateinit var effectSpinner: Spinner
    private lateinit var applyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        patternSpinner = findViewById(R.id.patternSpinner)
        colorSpinner = findViewById(R.id.colorSpinner)
        directionSpinner = findViewById(R.id.directionSpinner)
        effectSpinner = findViewById(R.id.effectSpinner)
        applyButton = findViewById(R.id.applyButton)

        applyButton.setOnClickListener {
            val prefs = getSharedPreferences("WallpaperPrefs", MODE_PRIVATE)
            prefs.edit()
                .putString("pattern", patternSpinner.selectedItem.toString())
                .putString("color", colorSpinner.selectedItem.toString())
                .putString("direction", directionSpinner.selectedItem.toString())
                .putString("effect", effectSpinner.selectedItem.toString())
                .apply()

            Toast.makeText(this, "Wallpaper applied!", Toast.LENGTH_SHORT).show()

            // استدعاء خدمة الخلفية
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
        }
    }
}
