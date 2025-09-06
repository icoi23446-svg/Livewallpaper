package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        // عناصر الواجهة
        val spinnerPattern = findViewById<Spinner>(R.id.spinnerPattern)
        val spinnerColor = findViewById<Spinner>(R.id.spinnerColor)
        val spinnerDirection = findViewById<Spinner>(R.id.spinnerDirection)
        val spinnerEffect = findViewById<Spinner>(R.id.spinnerEffect)

        val seekSpeed = findViewById<SeekBar>(R.id.seekSpeed)
        val seekSize = findViewById<SeekBar>(R.id.seekSize)
        val seekDensity = findViewById<SeekBar>(R.id.seekDensity)

        val btnApply = findViewById<Button>(R.id.btnApply)

        // تحميل الـ arrays من strings.xml
        setupSpinner(spinnerPattern, R.array.patterns, "pattern")
        setupSpinner(spinnerColor, R.array.colors, "color")
        setupSpinner(spinnerDirection, R.array.directions, "direction")
        setupSpinner(spinnerEffect, R.array.effects, "effect")

        setupSeekBar(seekSpeed, "speed")
        setupSeekBar(seekSize, "size")
        setupSeekBar(seekDensity, "density")

        // زر التطبيق
        btnApply.setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this, MultiEngineService::class.java)
                )
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "تعذر فتح إعدادات الخلفية", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayRes: Int, key: String) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayRes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // استرجاع القيمة المحفوظة
        val saved = prefs.getString(key, null)
        if (saved != null) {
            val pos = adapter.getPosition(saved)
            if (pos >= 0) spinner.setSelection(pos)
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val value = parent?.getItemAtPosition(position).toString()
                prefs.edit().putString(key, value).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSeekBar(seekBar: SeekBar, key: String) {
        val saved = prefs.getInt(key, -1)
        if (saved >= 0) {
            seekBar.progress = saved
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seek: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt(key, progress).apply()
            }

            override fun onStartTrackingTouch(seek: SeekBar?) {}
            override fun onStopTrackingTouch(seek: SeekBar?) {}
        })
    }
}
