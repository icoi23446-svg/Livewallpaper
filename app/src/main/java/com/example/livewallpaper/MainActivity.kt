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

        prefs = getSharedPreferences("WallpaperSettings", MODE_PRIVATE)

        // === عناصر الواجهة ===
        val patternSpinner = findViewById<Spinner>(R.id.spinnerPattern)
        val colorSpinner = findViewById<Spinner>(R.id.spinnerColor)
        val directionSpinner = findViewById<Spinner>(R.id.spinnerDirection)
        val effectSpinner = findViewById<Spinner>(R.id.spinnerEffect)

        val speedSeek = findViewById<SeekBar>(R.id.seekSpeed)
        val sizeSeek = findViewById<SeekBar>(R.id.seekSize)
        val densitySeek = findViewById<SeekBar>(R.id.seekDensity)

        // === إعداد الـ Spinners بالقيم ===
        patternSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Animated Gradient", "Color Cycle", "Particles", "Waves")
        )

        colorSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("أزرق", "أحمر", "أخضر", "أصفر", "بنفسجي", "سماوي", "عشوائي")
        )

        directionSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("يمين", "يسار", "أعلى", "أسفل")
        )

        effectSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("بدون", "وميض", "دوران", "شفافية")
        )

        // === حدود الـ SeekBars ===
        speedSeek.max = 10
        sizeSeek.max = 100
        densitySeek.max = 10

        // === تحميل القيم المحفوظة (عند فتح التطبيق) ===
        patternSpinner.setSelection(
            (patternSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("pattern", "Animated Gradient")
            )
        )
        colorSpinner.setSelection(
            (colorSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("color", "أزرق")
            )
        )
        directionSpinner.setSelection(
            (directionSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("direction", "يمين")
            )
        )
        effectSpinner.setSelection(
            (effectSpinner.adapter as ArrayAdapter<String>).getPosition(
                prefs.getString("effect", "بدون")
            )
        )

        speedSeek.progress = prefs.getInt("speed", 5)
        sizeSeek.progress = prefs.getInt("size", 50)
        densitySeek.progress = prefs.getInt("density", 5)

        // === حفظ التغييرات عند تغيير المستخدم ===
        patternSpinner.onItemSelectedListener = simpleSave { value ->
            prefs.edit().putString("pattern", value).apply()
        }
        colorSpinner.onItemSelectedListener = simpleSave { value ->
            prefs.edit().putString("color", value).apply()
        }
        directionSpinner.onItemSelectedListener = simpleSave { value ->
            prefs.edit().putString("direction", value).apply()
        }
        effectSpinner.onItemSelectedListener = simpleSave { value ->
            prefs.edit().putString("effect", value).apply()
        }

        speedSeek.setOnSeekBarChangeListener(saveInt("speed"))
        sizeSeek.setOnSeekBarChangeListener(saveInt("size"))
        densitySeek.setOnSeekBarChangeListener(saveInt("density"))
    }

    // Listener مبسط لحفظ النصوص من Spinners
    private fun simpleSave(save: (String) -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?, view: android.view.View?,
            position: Int, id: Long
        ) {
            val value = parent?.getItemAtPosition(position).toString()
            save(value)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    // Listener مبسط لحفظ الأرقام من SeekBars
    private fun saveInt(key: String) = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            prefs.edit().putInt(key, progress).apply()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}
        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    }
}
