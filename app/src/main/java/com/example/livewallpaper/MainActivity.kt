package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerPattern: Spinner
    private lateinit var seekSpeed: SeekBar
    private lateinit var etColor1: EditText
    private lateinit var etColor2: EditText
    private lateinit var btnApply: Button
    private lateinit var btnRandom: Button
    private lateinit var tvInfo: TextView

    private val patterns = arrayOf(
        "Gradient Shift",
        "Aurora / Nebula",
        "Blobs",
        "Particles",
        "Conic Spin"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerPattern = findViewById(R.id.spinnerPattern)
        seekSpeed = findViewById(R.id.seekSpeed)
        etColor1 = findViewById(R.id.etColor1)
        etColor2 = findViewById(R.id.etColor2)
        btnApply = findViewById(R.id.btnApply)
        btnRandom = findViewById(R.id.btnRandom)
        tvInfo = findViewById(R.id.tvInfo)

        // إعداد القائمة المنسدلة بالأنماط
        spinnerPattern.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, patterns)

        // زر لاختيار ألوان عشوائية
        btnRandom.setOnClickListener {
            val c1 = randomColor()
            val c2 = randomColor()
            etColor1.setText(colorToHex(c1))
            etColor2.setText(colorToHex(c2))
        }

        // زر تطبيق الإعدادات وتفعيل الخلفية الحية
        btnApply.setOnClickListener {
            val prefs = getSharedPreferences("live_prefs", MODE_PRIVATE).edit()
            prefs.putInt("pattern", spinnerPattern.selectedItemPosition)
            prefs.putString("color1", etColor1.text.toString())
            prefs.putString("color2", etColor2.text.toString())
            prefs.putInt("speed", seekSpeed.progress)
            prefs.apply()

            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, MultiEngineService::class.java)
                )
            }
            startActivity(intent)
        }
    }

    private fun randomColor(): Int =
        Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))

    private fun colorToHex(c: Int): String =
        String.format("#%06X", 0xFFFFFF and c)
}
