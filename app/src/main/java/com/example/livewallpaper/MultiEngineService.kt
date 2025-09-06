package com.example.livewallpaper

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("LiveWallpaperPrefs", MODE_PRIVATE)

        // عناصر الواجهة
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val speedSeek: SeekBar = findViewById(R.id.speedSeekBar)
        val sizeSeek: SeekBar = findViewById(R.id.sizeSeekBar)
        val densitySeek: SeekBar = findViewById(R.id.densitySeekBar)
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)

        // تحميل القيم المحفوظة سابقاً
        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)

        // عند تغيير القيمة يتم حفظها
        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                prefs.edit().putInt("speed", value).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                prefs.edit().putInt("size", value).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        densitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                prefs.edit().putInt("density", value).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // سبينرات (نمط – لون – اتجاه – تأثير)
        patternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position).toString()
                prefs.edit().putString("pattern", value).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position).toString()
                prefs.edit().putString("color", value).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        directionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position).toString()
                prefs.edit().putString("direction", value).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        effectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                val value = parent.getItemAtPosition(position).toString()
                prefs.edit().putString("effect", value).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }
}
