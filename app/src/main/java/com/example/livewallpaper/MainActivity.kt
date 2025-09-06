package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
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

        val speedSeek = findViewById<SeekBar>(R.id.speedSeek)
        val sizeSeek = findViewById<SeekBar>(R.id.sizeSeek)
        val densitySeek = findViewById<SeekBar>(R.id.densitySeek)

        val applyButton = findViewById<Button>(R.id.applyButton)

        // تحميل القوائم من arrays.xml
        patternSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.patterns_array, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        colorSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.colors_array, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        directionSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.direction_array, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        effectSpinner.adapter = ArrayAdapter.createFromResource(
            this, R.array.effects_array, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        // حفظ القيم عند التغيير
        patternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("pattern", parent?.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("color", parent?.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        directionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("direction", parent?.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        effectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("effect", parent?.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // SeekBars
        speedSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("speed", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        sizeSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("size", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        densitySeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("density", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // زر التطبيق
        applyButton.setOnClickListener {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@MainActivity, MultiEngineService::class.java)
                )
            }
            startActivity(intent)
        }
    }
}
