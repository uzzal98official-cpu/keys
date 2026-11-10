package com.minimalkey.ime

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("minimal_key", Context.MODE_PRIVATE) }
    private val imageRequest = 41

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dark = prefs.getString("mode", "system") == "dark"
        val bg = if (dark) Color.rgb(25, 27, 32) else Color.rgb(247, 245, 250)
        val fg = if (dark) Color.WHITE else Color.rgb(30, 27, 36)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
            setBackgroundColor(bg)
        }
        fun label(text: String, size: Float = 16f) = TextView(this).apply {
            this.text = text; textSize = size; setTextColor(fg)
            setPadding(0, 12, 0, 8)
        }
        page.addView(label("Minimal Key", 28f))
        page.addView(label("A clean, lightweight keyboard. Customize the look, then enable it in Android settings."))
        page.addView(label("Appearance", 20f))

        val mode = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item,
                listOf("System", "Light", "Dark"))
            setSelection(when (prefs.getString("mode", "system")) { "light" -> 1; "dark" -> 2; else -> 0 })
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    prefs.edit().putString("mode", listOf("system", "light", "dark")[position]).apply()
                }
            }
        }
        page.addView(label("Theme mode")); page.addView(mode)

        val palette = Spinner(this).apply {
            val names = listOf("Material Purple", "Ocean Blue", "Forest Green", "Rose")
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, names)
            val saved = prefs.getString("palette", "purple")
            setSelection(listOf("purple","blue","green","rose").indexOf(saved).coerceAtLeast(0))
            onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    prefs.edit().putString("palette", listOf("purple","blue","green","rose")[position]).apply()
                }
            }
        }
        page.addView(label("Accent theme")); page.addView(palette)

        page.addView(label("Keyboard height", 20f))
        val height = SeekBar(this).apply {
            max = 120
            progress = (prefs.getInt("height_dp", 250) - 190).coerceIn(0, 120)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    prefs.edit().putInt("height_dp", 190 + progress).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        page.addView(height)
        page.addView(label("Keyboard height: 190–310 dp. Width follows the screen width."))

        val image = Button(this).apply {
            text = "Choose keyboard background image"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"; addCategory(Intent.CATEGORY_OPENABLE)
                }
                startActivityForResult(intent, imageRequest)
            }
        }
        page.addView(image)
        page.addView(Button(this).apply {
            text = "Remove background image"
            setOnClickListener { prefs.edit().remove("background_uri").apply(); Toast.makeText(this@MainActivity, "Background removed", Toast.LENGTH_SHORT).show() }
        })

        page.addView(label("Enable Minimal Key", 20f))
        page.addView(Button(this).apply {
            text = "Open keyboard settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })
        page.addView(Button(this).apply {
            text = "Select Minimal Key"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        })
        page.addView(label("Privacy: this starter build has no internet permission and does not send typed text anywhere."))
        page.addView(label("Note: Android may show a system warning when enabling any third-party keyboard."))
        setContentView(ScrollView(this).apply { addView(page) })
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageRequest && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                prefs.edit().putString("background_uri", uri.toString()).apply()
                Toast.makeText(this, "Background saved. Reopen the keyboard.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
