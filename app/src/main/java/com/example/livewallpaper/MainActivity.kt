package com.example.livewallpaper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        // عناصر الواجهة
        val patternSpinner: Spinner = findViewById(R.id.patternSpinner)
        val colorSpinner: Spinner = findViewById(R.id.colorSpinner)
        val directionSpinner: Spinner = findViewById(R.id.directionSpinner)
        val effectSpinner: Spinner = findViewById(R.id.effectSpinner)

        val speedSlider: Slider = findViewById(R.id.speedSlider)
        val sizeSlider: Slider = findViewById(R.id.sizeSlider)
        val densitySlider: Slider = findViewById(R.id.densitySlider)

        val applyBtn: Button = findViewById(R.id.applyBtn)

        // تحميل البيانات من strings.xml
        ArrayAdapter.createFromResource(
            this,
            R.array.patterns_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            patternSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.colors_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            colorSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.direction_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            directionSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.effects_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            effectSpinner.adapter = adapter
        }

        // تحميل القيم السابقة (لو موجودة)
        patternSpinner.setSelection(getIndex(patternSpinner, prefs.getString("pattern", "تدرج لوني")))
        colorSpinner.setSelection(getIndex(colorSpinner, prefs.getString("color", "أزرق")))
        directionSpinner.setSelection(getIndex(directionSpinner, prefs.getString("direction", "يمين")))
        effectSpinner.setSelection(getIndex(effectSpinner, prefs.getString("effect", "بدون")))

        speedSlider.value = prefs.getInt("speed", 5).toFloat()
        sizeSlider.value = prefs.getInt("size", 50).toFloat()
        densitySlider.value = prefs.getInt("density", 5).toFloat()

        // حفظ التغييرات فورًا عند التغيير
        patternSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("pattern", parent.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("color", parent.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        directionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("direction", parent.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        effectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, position: Int, id: Long) {
                prefs.edit().putString("effect", parent.getItemAtPosition(position).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        speedSlider.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("speed", value.toInt()).apply()
        }

        sizeSlider.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("size", value.toInt()).apply()
        }

        densitySlider.addOnChangeListener { _, value, _ ->
            prefs.edit().putInt("density", value.toInt()).apply()
        }

        // زر تطبيق الخلفية
        applyBtn.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_LIVE_WALLPAPER_CHOOSER)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "يرجى تعيين الخلفية الحية من الإعدادات", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getIndex(spinner: Spinner, value: String?): Int {
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString().equals(value, ignoreCase = true)) {
                return i
            }
        }
        return 0
    }
}
