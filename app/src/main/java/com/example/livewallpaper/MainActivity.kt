package com.example.livewallpaper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        // 🔹 Spinner لاختيار النمط
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val patterns = arrayOf(
            "Animated Gradient",
            "Smooth Gradient",   // ✅ الجديد (تدرج أنعم)
            "Color Cycle",
            "Particles",
            "Waves"
        )
        val patternAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, patterns)
        patternAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        patternSpinner.adapter = patternAdapter
        patternSpinner.setSelection(patterns.indexOf(prefs.getString("pattern", "Animated Gradient")))
        patternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("pattern", patterns[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔹 Spinner لاختيار اللون
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val colors = arrayOf("أزرق", "أحمر", "أخضر", "أصفر", "بنفسجي", "سماوي", "عشوائي")
        val colorAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, colors)
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        colorSpinner.adapter = colorAdapter
        colorSpinner.setSelection(colors.indexOf(prefs.getString("color", "أزرق")))
        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("color", colors[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔹 Slider السرعة
        val speedSeek: SeekBar = findViewById(R.id.speedSeek)
        speedSeek.progress = prefs.getInt("speed", 5)
        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("speed", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 🔹 Slider الحجم
        val sizeSeek: SeekBar = findViewById(R.id.sizeSeek)
        sizeSeek.progress = prefs.getInt("size", 50)
        sizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("size", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 🔹 Slider الكثافة
        val densitySeek: SeekBar = findViewById(R.id.densitySeek)
        densitySeek.progress = prefs.getInt("density", 5)
        densitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("density", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 🔹 Spinner الاتجاه
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val directions = arrayOf("يمين", "يسار", "أعلى", "أسفل")
        val directionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, directions)
        directionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        directionSpinner.adapter = directionAdapter
        directionSpinner.setSelection(directions.indexOf(prefs.getString("direction", "يمين")))
        directionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("direction", directions[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔹 Spinner التأثير
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)
        val effects = arrayOf("بدون", "وميض", "دوران", "شفافية")
        val effectAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, effects)
        effectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        effectSpinner.adapter = effectAdapter
        effectSpinner.setSelection(effects.indexOf(prefs.getString("effect", "بدون")))
        effectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("effect", effects[position]).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 🔹 زر لتعيين الخلفية الحية
        val setWallpaperBtn: Button = findViewById(R.id.setWallpaperBtn)
        setWallpaperBtn.setOnClickListener {
            try {
                val intent = Intent(WallpaperService.SERVICE_INTERFACE)
                intent.setClass(this, MultiEngineService::class.java)
                startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
            } catch (e: Exception) {
                Toast.makeText(this, "تعذّر فتح إعدادات الخلفية", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
            }
        }
    }
}
