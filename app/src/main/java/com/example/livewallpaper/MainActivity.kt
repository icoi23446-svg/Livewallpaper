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

        // الـ Spinners
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)

        // الـ SeekBars
        val speedSeek: SeekBar = findViewById(R.id.speedSeek)
        val sizeSeek: SeekBar = findViewById(R.id.sizeSeek)
        val densitySeek: SeekBar = findViewById(R.id.densitySeek)

        // زر التعيين
        val applyBtn: Button = findViewById(R.id.applyBtn)

        // ربط الـ spinners بالـ arrays في strings.xml
        patternSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.patterns, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        colorSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.colors, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        directionSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.directions, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        effectSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.effects, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // تحميل القيم المخزنة عند فتح التطبيق
        loadSettings(patternSpinner, colorSpinner, directionSpinner, effectSpinner, speedSeek, sizeSeek, densitySeek)

        // حفظ القيم عند التغيير
        patternSpinner.onItemSelectedListener = saveString("pattern")
        colorSpinner.onItemSelectedListener = saveString("color")
        directionSpinner.onItemSelectedListener = saveString("direction")
        effectSpinner.onItemSelectedListener = saveString("effect")

        speedSeek.setOnSeekBarChangeListener(saveInt("speed"))
        sizeSeek.setOnSeekBarChangeListener(saveInt("size"))
        densitySeek.setOnSeekBarChangeListener(saveInt("density"))

        // عند الضغط على زر "تعيين"
        applyBtn.setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                    putExtra(
                        WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                        android.content.ComponentName(this@MainActivity, MultiEngineService::class.java)
                    )
                }
                startActivity(intent)
            } catch (e: Exception) {
                // fallback في حالة عدم دعم الجهاز
                startActivity(Intent(Settings.ACTION_WALLPAPER_SETTINGS))
            }
        }
    }

    // حفظ String
    private fun saveString(key: String) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
            val value = parent.getItemAtPosition(position).toString()
            prefs.edit().putString(key, value).apply()
        }

        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    // حفظ Int
    private fun saveInt(key: String) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            prefs.edit().putInt(key, progress).apply()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    // تحميل الإعدادات المخزنة
    private fun loadSettings(
        patternSpinner: Spinner, colorSpinner: Spinner, directionSpinner: Spinner, effectSpinner: Spinner,
        speedSeek: SeekBar, sizeSeek: SeekBar, densitySeek: SeekBar
    ) {
        patternSpinner.setSelection((patternSpinner.adapter as ArrayAdapter<String>).getPosition(prefs.getString("pattern", "تدرج لوني")))
        colorSpinner.setSelection((colorSpinner.adapter as ArrayAdapter<String>).getPosition(prefs.getString("color", "أزرق")))
        directionSpinner.setSelection((directionSpinner.adapter as ArrayAdapter<String>).getPosition(prefs.getString("direction", "يمين")))
        effectSpinner.setSelection((effectSpinner.adapter as ArrayAdapter<String>).getPosition(prefs.getString("effect", "بدون")))

        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)
    }
}
