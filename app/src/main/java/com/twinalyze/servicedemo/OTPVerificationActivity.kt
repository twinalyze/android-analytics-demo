package com.twinalyze.servicedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.twinalyze.alldatget.AllScreenTracker
import com.twinalyze.event.SetAnalytics

class OTPVerificationActivity : AppCompatActivity() {

    private lateinit var btnVerify: Button
    private lateinit var boxes: Array<EditText>

    private fun View.showKeyboard() {
        requestFocus()
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_otpverification)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SetAnalytics.getInstance()
            .setActivityEvent(
                "OTPVerificationActivity",    // screenName
                this@OTPVerificationActivity // screenClass
            )

        val e1 = findViewById<EditText>(R.id.et1)
        val e2 = findViewById<EditText>(R.id.et2)
        val e3 = findViewById<EditText>(R.id.et3)
        val e4 = findViewById<EditText>(R.id.et4)
        val e5 = findViewById<EditText>(R.id.et5)
        val e6 = findViewById<EditText>(R.id.et6)
        boxes = arrayOf(e1, e2, e3, e4, e5, e6)

        e1.showKeyboard()

        btnVerify = findViewById(R.id.btn_verify)

        setupOtpFlow(*boxes)

         fun EditText.autoMoveTo(next: EditText?) {
            addTextChangedListener(object : android.text.TextWatcher {
                override fun afterTextChanged(s: android.text.Editable?) {
                    if (s?.length == 1) next?.requestFocus()
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

// after findViewById for et1..et6:
        e1.autoMoveTo(e2)
        e2.autoMoveTo(e3)
        e3.autoMoveTo(e4)
        e4.autoMoveTo(e5)
        e5.autoMoveTo(e6)


        btnVerify.setOnClickListener {
            val t0 = android.os.SystemClock.uptimeMillis()
            val code = otpCode()

            if (code.length == 6) {
                android.util.Log.d("TRACE", "Verify click, before startActivity")

                startActivity(Intent(this, MainActivity::class.java))

                android.util.Log.d(
                    "TRACE",
                    "After startActivity, total=${android.os.SystemClock.uptimeMillis() - t0}ms"
                )
            } else {
                Toast.makeText(this, "Please enter 6-digit code", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun setupOtpFlow(vararg boxes: EditText) {
        boxes.forEachIndexed { i, et ->
            et.doOnTextChanged { text, _, _, _ ->
                val s = text?.toString().orEmpty()
                if (i == 0 && s.length > 1) {
                    val digits = s.filter { it.isDigit() }.take(boxes.size)
                    digits.forEachIndexed { idx, c -> boxes[idx].setText(c.toString()) }
                    boxes[minOf(digits.length, boxes.lastIndex)].requestFocus()
                } else if (s.length == 1 && i < boxes.lastIndex) {
                    boxes[i + 1].requestFocus()
                }
                updateVerifyVisual() // just visual, not disabling
            }

            et.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    et.text.isEmpty() && i > 0
                ) {
                    boxes[i - 1].apply { requestFocus(); text?.clear() }
                    updateVerifyVisual()
                    true
                } else false
            }
        }
    }

    // Visual cue only (dim when not ready)
    private fun updateVerifyVisual() {
        val ready = boxes.all { it.text?.length == 1 }
//        btnVerify.alpha = if (ready) 1f else 0.5f
        btnVerify.alpha = if (ready) 1f else 1f
        // keep the button enabled/clickable so we can show the toast
    }

    private fun otpCode(): String = boxes.joinToString("") { it.text.toString() }

    override fun onResume() {
        super.onResume()
        if (AllScreenTracker.getInstance().isManualAppForeground) {
            SetAnalytics.getInstance()
                .setActivityEvent(
                    "OTPVerificationActivity Foreground Manual", // screenName
                    this@OTPVerificationActivity            // screenClass
                )
        }
    }
}