package com.example.livewallpaper

import android.content.Context
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        // اربط العناصر من XML
        val spinnerPattern: Spinner = findViewById(R.id.spinnerPattern)
        val spinnerColor: Spinner = findViewById(R.id.spinnerColor)
        val spinnerDirection: Spinner = findViewById(R.id.spinnerDirection)
        val spinnerEffect: Spinner = findViewById(R.id.spinnerEffect)

        val seekSpeed: SeekBar = findViewById(R.id.seekSpeed)
        val seekSize: SeekBar = findViewById(R.id.seekSize)
        val seekDensity: SeekBar = findViewById(R.id.seekDensity)

        // القوائم
        val patterns = listOf("Animated Gradient", "Color Cycle", "Particles", "Waves")
        val colors = listOf("أحمر", "أزرق", "أخضر", "أصفر", "بنفسجي", "سماوي", "عشوائي")
        val directions = listOf("يمين", "يسار", "أعلى", "أسفل")
        val effects = listOf("بدون", "وميض", "دوران", "شفافية")

        // adapters
        spinnerPattern.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, patterns)
        spinnerColor.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, colors)
        spinnerDirection.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, directions)
        spinnerEffect.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, effects)

        // تحميل القيم المحفوظة
        spinnerPattern.setSelection(patterns.indexOf(prefs.getString("pattern", "Animated Gradient")))
        spinnerColor.setSelection(colors.indexOf(prefs.getString("color", "أزرق")))
        spinnerDirection.setSelection(directions.indexOf(prefs.getString("direction", "يمين")))
        spinnerEffect.setSelection(effects.indexOf(prefs.getString("effect", "بدون")))

        seekSpeed.progress = prefs.getInt("speed", 5)
        seekSize.progress = prefs.getInt("size", 50)
        seekDensity.progress = prefs.getInt("density", 5)

        // listeners للحفظ عند التغيير
        spinnerPattern.setOnItemSelectedListener(SimpleItemSelectedListener { value ->
            prefs.edit().putString("pattern", value).apply()
        })
        spinnerColor.setOnItemSelectedListener(SimpleItemSelectedListener { value ->
            prefs.edit().putString("color", value).apply()
        })
        spinnerDirection.setOnItemSelectedListener(SimpleItemSelectedListener { value ->
            prefs.edit().putString("direction", value).apply()
        })
        spinnerEffect.setOnItemSelectedListener(SimpleItemSelectedListener { value ->
            prefs.edit().putString("effect", value).apply()
        })

        seekSpeed.setOnSeekBarChangeListener(SimpleSeekBarListener { value ->
            prefs.edit().putInt("speed", value).apply()
        })
        seekSize.setOnSeekBarChangeListener(SimpleSeekBarListener { value ->
            prefs.edit().putInt("size", value).apply()
        })
        seekDensity.setOnSeekBarChangeListener(SimpleSeekBarListener { value ->
            prefs.edit().putInt("density", value).apply()
        })
    }
}

// Listener بسيط للـ Spinner
class SimpleItemSelectedListener(val callback: (String) -> Unit) : android.widget.AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
        val value = parent?.getItemAtPosition(position).toString()
        callback(value)
    }
    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
}

// Listener بسيط للـ SeekBar
class SimpleSeekBarListener(val callback: (Int) -> Unit) : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        callback(progress)
    }
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
