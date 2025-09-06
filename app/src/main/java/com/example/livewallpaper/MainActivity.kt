package com.example.livewallpaper

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val prefs = getSharedPreferences("WallpaperSettings", Context.MODE_PRIVATE)

        // عناصر الواجهة
        val spinnerPattern: Spinner = findViewById(R.id.spinnerPattern)
        val spinnerColor: Spinner = findViewById(R.id.spinnerColor)
        val spinnerDirection: Spinner = findViewById(R.id.spinnerDirection)
        val spinnerEffect: Spinner = findViewById(R.id.spinnerEffect)
        val seekSpeed: SeekBar = findViewById(R.id.seekSpeed)
        val seekSize: SeekBar = findViewById(R.id.seekSize)
        val seekDensity: SeekBar = findViewById(R.id.seekDensity)
        val btnApply: Button = findViewById(R.id.btnApply)

        // تحميل القوائم من strings.xml
        ArrayAdapter.createFromResource(
            this, R.array.patterns_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerPattern.adapter = it }

        ArrayAdapter.createFromResource(
            this, R.array.colors_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerColor.adapter = it }

        ArrayAdapter.createFromResource(
            this, R.array.direction_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerDirection.adapter = it }

        ArrayAdapter.createFromResource(
            this, R.array.effects_array, android.R.layout.simple_spinner_item
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); spinnerEffect.adapter = it }

        // استرجاع آخر قيم
        spinnerPattern.setSelection((spinnerPattern.adapter as ArrayAdapter<String>).getPosition(prefs.getString("pattern", "Animated Gradient")))
        spinnerColor.setSelection((spinnerColor.adapter as ArrayAdapter<String>).getPosition(prefs.getString("color", "أزرق")))
        spinnerDirection.setSelection((spinnerDirection.adapter as ArrayAdapter<String>).getPosition(prefs.getString("direction", "يمين")))
        spinnerEffect.setSelection((spinnerEffect.adapter as ArrayAdapter<String>).getPosition(prefs.getString("effect", "بدون")))

        seekSpeed.progress = prefs.getInt("speed", 5)
        seekSize.progress = prefs.getInt("size", 50)
        seekDensity.progress = prefs.getInt("density", 5)

        // حفظ عند التغيير
        spinnerPattern.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("pattern", parent.getItemAtPosition(pos).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("color", parent.getItemAtPosition(pos).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerDirection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("direction", parent.getItemAtPosition(pos).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        spinnerEffect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: android.view.View?, pos: Int, id: Long) {
                prefs.edit().putString("effect", parent.getItemAtPosition(pos).toString()).apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("speed", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("size", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekDensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                prefs.edit().putInt("density", progress).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // زر تطبيق الخلفية
        btnApply.setOnClickListener {
            try {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
                intent.putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this, MultiEngineService::class.java)
                )
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "خطأ: لم أستطع فتح إعدادات الخلفية", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
