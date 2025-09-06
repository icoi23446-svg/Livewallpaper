package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
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

        prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        val patternSpinner = findViewById<Spinner>(R.id.patternSpinner)
        val colorSpinner = findViewById<Spinner>(R.id.colorSpinner)
        val directionSpinner = findViewById<Spinner>(R.id.directionSpinner)
        val effectSpinner = findViewById<Spinner>(R.id.effectSpinner)

        val speedSeek = findViewById<SeekBar>(R.id.speedSeekBar)
        val sizeSeek = findViewById<SeekBar>(R.id.sizeSeekBar)
        val densitySeek = findViewById<SeekBar>(R.id.densitySeekBar)

        val applyButton = findViewById<Button>(R.id.applyButton)

        // adapters
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

        // تحميل القيم المحفوظة
        setSpinnerSelection(patternSpinner, prefs.getString("pattern", null))
        setSpinnerSelection(colorSpinner, prefs.getString("color", null))
        setSpinnerSelection(directionSpinner, prefs.getString("direction", null))
        setSpinnerSelection(effectSpinner, prefs.getString("effect", null))

        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)

        // Listeners للحفظ
        spinnerSave(patternSpinner, "pattern")
        spinnerSave(colorSpinner, "color")
        spinnerSave(directionSpinner, "direction")
        spinnerSave(effectSpinner, "effect")

        seekSave(speedSeek, "speed")
        seekSave(sizeSeek, "size")
        seekSave(densitySeek, "density")

        // زر التعيين
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

    private fun setSpinnerSelection(spinner: Spinner, value: String?) {
        if (value == null) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val pos = adapter.getPosition(value)
        if (pos >= 0) spinner.setSelection(pos)
    }

    private fun spinnerSave(spinner: Spinner, key: String) {
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long
            ) {
                val value = parent.getItemAtPosition(position).toString()
                prefs.edit().putString(key, value).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun seekSave(seekBar: SeekBar, key: String) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt(key, progress).apply()
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
}
