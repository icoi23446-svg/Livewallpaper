package com.example.livewallpaper

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)

        // المكونات
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)
        val themeSpinner: Spinner = findViewById(R.id.themeSpinner)

        val speedSeek: SeekBar = findViewById(R.id.speedSeek)
        val sizeSeek: SeekBar = findViewById(R.id.sizeSeek)
        val densitySeek: SeekBar = findViewById(R.id.densitySeek)

        val applyBtn: Button = findViewById(R.id.applyBtn)

        // تحميل القيم السابقة
        setSpinnerSelection(patternSpinner, prefs.getString("pattern", "Animated Gradient"))
        setSpinnerSelection(colorSpinner, prefs.getString("color", "أزرق"))
        setSpinnerSelection(directionSpinner, prefs.getString("direction", "يمين"))
        setSpinnerSelection(effectSpinner, prefs.getString("effect", "بدون"))
        setSpinnerSelection(themeSpinner, prefs.getString("theme", "تلقائي"))

        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)

        // حفظ عند التغيير
        patternSpinner.onItemSelectedListener = makeListener("pattern")
        colorSpinner.onItemSelectedListener = makeListener("color")
        directionSpinner.onItemSelectedListener = makeListener("direction")
        effectSpinner.onItemSelectedListener = makeListener("effect")

        speedSeek.setOnSeekBarChangeListener(makeSeekListener("speed"))
        sizeSeek.setOnSeekBarChangeListener(makeSeekListener("size"))
        densitySeek.setOnSeekBarChangeListener(makeSeekListener("density"))

        // الثيم
        themeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                prefs.edit().putString("theme", selected).apply()
                when (selected) {
                    "فاتح" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    "ليلي" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // زر تطبيق الخلفية
        applyBtn.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "تعذر فتح إعدادات الخلفية", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun makeListener(key: String) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            val selected = parent.getItemAtPosition(position).toString()
            prefs.edit().putString(key, selected).apply()
        }
        override fun onNothingSelected(parent: AdapterView<*>) {}
    }

    private fun makeSeekListener(key: String) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            prefs.edit().putInt(key, progress).apply()
        }
        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        val adapter = spinner.adapter
        if (adapter != null && value != null) {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).toString().equals(value, ignoreCase = true)) {
                    spinner.setSelection(i)
                    break
                }
            }
        }
    }
}
