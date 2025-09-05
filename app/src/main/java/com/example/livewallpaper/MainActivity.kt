package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    val patterns = arrayOf(
        "Gradient Shift",
        "Aurora / Nebula",
        "Blobs",
        "Particles",
        "Conic Spin"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerPattern.adapter = ArrayAdapter(
            this, 
            android.R.layout.simple_spinner_dropdown_item, 
            patterns
        )

        btnRandom.setOnClickListener {
            val c1 = randomColor()
            val c2 = randomColor()
            etColor1.setText(colorToHex(c1))
            etColor2.setText(colorToHex(c2))
        }

        btnApply.setOnClickListener {
            // Save preferences for the wallpaper engine
            val prefs = getSharedPreferences("live_prefs", MODE_PRIVATE)
            val editor = prefs.edit()
            editor.putInt("pattern", spinnerPattern.selectedItemPosition)
            editor.putString("color1", etColor1.text.toString())
            editor.putString("color2", etColor2.text.toString())
            editor.putInt("speed", seekSpeed.progress)
            editor.apply()

            // Launch change live wallpaper intent
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
            intent.putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(this, MultiEngineService::class.java)
            )
            startActivity(intent)
        }
    }

    private fun randomColor(): Int {
        val rnd = Random
        return Color.rgb(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    private fun colorToHex(c: Int): String {
        return String.format("#%06X", 0xFFFFFF and c)
    }
}
