package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // زرار وحيد
        val button = Button(this)
        button.text = "Set Live Wallpaper"
        setContentView(button)

        button.setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        ComponentName(this@MainActivity, MultiEngineService::class.java)
                    )
                }
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivity(intent)
            }
        }
    }
}
