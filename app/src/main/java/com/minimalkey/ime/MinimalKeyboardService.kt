package com.minimalkey.ime

import android.inputmethodservice.InputMethodService
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ImageView
import android.content.Context
import kotlin.math.roundToInt

class MinimalKeyboardService : InputMethodService() {
    private var spaceDownX = 0f
    private var cursorOffset = 0

    private fun dp(v: Int) = (v * resources.displayMetrics.density).roundToInt()
    private fun accent(): Int = when (getSharedPreferences("minimal_key", 0).getString("palette", "purple")) {
        "blue" -> Color.rgb(65, 105, 180)
        "green" -> Color.rgb(65, 125, 95)
        "rose" -> Color.rgb(180, 90, 120)
        else -> Color.rgb(103, 80, 164)
    }

    override fun onCreateInputView(): View {
        val prefs = getSharedPreferences("minimal_key", Context.MODE_PRIVATE)
        val mode = prefs.getString("mode", "system")
        val dark = when (mode) {
            "dark" -> true
            "light" -> false
            else -> (resources.configuration.uiMode and 0x30) == 0x20
        }
        val bg = if (dark) Color.rgb(29, 31, 36) else Color.rgb(238, 236, 243)
        val key = if (dark) Color.rgb(55, 57, 64) else Color.rgb(255, 255, 255)
        val fg = if (dark) Color.WHITE else Color.rgb(35, 33, 40)
        val height = dp(prefs.getInt("height_dp", 250))
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(3), dp(5), dp(3), dp(5))
            setBackgroundColor(bg)
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        }

        val uri = prefs.getString("background_uri", null)
        if (uri != null) {
            try {
                val image = ImageView(this).apply {
                    setImageURI(Uri.parse(uri)); scaleType = ImageView.ScaleType.CENTER_CROP
                    alpha = 0.16f
                }
                // Background image is intentionally subtle; keyboard keys remain legible.
                root.background = GradientDrawable().apply { setColor(bg) }
            } catch (_: Exception) {}
        }

        fun row(weight: Float = 1f) = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, weight).apply {
                setMargins(0, dp(2), 0, dp(2))
            }
        }
        fun key(text: String, value: String, weight: Float = 1f, special: Boolean = false): TextView {
            val radius = prefs.getInt("corner_radius", 9)
            return TextView(this).apply {
                this.text = text
                textSize = if (text.length > 1) 13f else 20f
                setTextColor(fg)
                gravity = Gravity.CENTER
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                background = GradientDrawable().apply {
                    setColor(if (special) accent() else key)
                    cornerRadius = dp(radius).toFloat()
                }
                val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                    setMargins(dp(2), dp(1), dp(2), dp(1))
                }
                layoutParams = lp
                isAllCaps = false
                setOnClickListener {
                    val ic = currentInputConnection ?: return@setOnClickListener
                    when (value) {
                        "BACK" -> ic.deleteSurroundingText(1, 0)
                        "ENTER" -> ic.sendKeyEvent(android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER))
                        "SHIFT" -> {}
                        "SPACE" -> ic.commitText(" ", 1)
                        else -> ic.commitText(value, 1)
                    }
                    animate().scaleX(0.96f).scaleY(0.96f).setDuration(55).withEndAction {
                        animate().scaleX(1f).scaleY(1f).setDuration(75).start()
                    }.start()
                }
            }
        }

        // Number row
        val nums = row()
        "1234567890".forEach { nums.addView(key(it.toString(), it.toString())) }
        root.addView(nums)

        listOf("qwertyuiop", "asdfghjkl", "zxcvbnm").forEachIndexed { index, letters ->
            val r = row()
            if (index == 1) {
                val spacer = View(this); r.addView(spacer, LinearLayout.LayoutParams(0, 1, 0.45f))
            }
            if (index == 2) r.addView(key("⇧", "SHIFT", 1.25f, true))
            letters.forEach { r.addView(key(it.toString(), it.toString())) }
            if (index == 1) {
                val spacer = View(this); r.addView(spacer, LinearLayout.LayoutParams(0, 1, 0.45f))
            }
            if (index == 2) r.addView(key("⌫", "BACK", 1.25f, true))
            root.addView(r)
        }

        val bottom = row()
        bottom.addView(key("?123", "?", 1.35f, true))
        val space = key("English (US)", "SPACE", 5.2f)
        space.textSize = 13f
        space.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> { spaceDownX = event.x; cursorOffset = 0; true }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - spaceDownX
                    val steps = (dx / dp(12)).toInt()
                    if (steps != cursorOffset) {
                        currentInputConnection?.sendKeyEvent(android.view.KeyEvent(
                            android.view.KeyEvent.ACTION_DOWN,
                            if (steps > cursorOffset) android.view.KeyEvent.KEYCODE_DPAD_RIGHT else android.view.KeyEvent.KEYCODE_DPAD_LEFT
                        ))
                        cursorOffset = steps
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (kotlin.math.abs(event.x - spaceDownX) < dp(8)) currentInputConnection?.commitText(" ", 1)
                    true
                }
                else -> true
            }
        }
        bottom.addView(space)
        bottom.addView(key(",", ",", 1f))
        bottom.addView(key("↵", "ENTER", 1.25f, true))
        root.addView(bottom)
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }
}
