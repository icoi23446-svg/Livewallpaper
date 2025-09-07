package com.example.livewallpaper

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var patternSpinner: Spinner
    private lateinit var colorSpinner: Spinner
    private lateinit var directionSpinner: Spinner
    private lateinit var effectSpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperPrefs", Context.MODE_PRIVATE)

        // ربط الـ Spinners
        patternSpinner = findViewById(R.id.patternSpinner)
        colorSpinner = findViewById(R.id.colorSpinner)
        directionSpinner = findViewById(R.id.directionSpinner)
        effectSpinner = findViewById(R.id.effectSpinner)

        // إعداد الأدابتور من الموارد
        setupSpinner(patternSpinner, R.array.patterns)
        setupSpinner(colorSpinner, R.array.colors)
        setupSpinner(directionSpinner, R.array.directions)
        setupSpinner(effectSpinner, R.array.effects)

        // تحميل القيم المخزنة
        setSpinnerSelection(patternSpinner, prefs.getString("pattern", null) ?: "")
        setSpinnerSelection(colorSpinner, prefs.getString("color", null) ?: "")
        setSpinnerSelection(directionSpinner, prefs.getString("direction", null) ?: "")
        setSpinnerSelection(effectSpinner, prefs.getString("effect", null) ?: "")

        // زر الحفظ
        val applyButton: Button = findViewById(R.id.applyButton)
        applyButton.setOnClickListener {
            val editor = prefs.edit()
            editor.putString("pattern", patternSpinner.selectedItem.toString())
            editor.putString("color", colorSpinner.selectedItem.toString())
            editor.putString("direction", directionSpinner.selectedItem.toString())
            editor.putString("effect", effectSpinner.selectedItem.toString())
            editor.apply()
        }
    }

    private fun setupSpinner(spinner: Spinner, arrayRes: Int) {
        val adapter = ArrayAdapter.createFromResource(
            this,
            arrayRes,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        if (value.isEmpty()) return
        val adapter = spinner.adapter as ArrayAdapter<String>
        val pos = adapter.getPosition(value)
        if (pos >= 0) spinner.setSelection(pos)
    }
}
