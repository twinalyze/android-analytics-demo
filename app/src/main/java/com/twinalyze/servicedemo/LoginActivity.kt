package com.twinalyze.servicedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doOnTextChanged
import com.twinalyze.alldatget.AllScreenTracker
import com.twinalyze.event.SetAnalytics

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SetAnalytics.getInstance()
            .setActivityEvent(
                "LoginActivity Manual",    // screenName
                this@LoginActivity // screenClass
            )

        val etNumber = findViewById<EditText>(R.id.et_number)
        val btnGetOtp = findViewById<Button>(R.id.btn_get_otp)
        val btnSkip = findViewById<Button>(R.id.btn_skip)

        etNumber.showKeyboard()

       btnGetOtp.setOnClickListener {

           val number = etNumber.text.toString().trim()
           val isValid = number.length == 10 && number.all { it.isDigit() }
           if (isValid) {
               startActivity(
                   Intent(this, OTPVerificationActivity::class.java)
                       .putExtra("mobile", number)
               )
           } else {
               Toast.makeText(this, "Please enter a valid 10-digit mobile", Toast.LENGTH_SHORT).show()
               etNumber.requestFocus()
           }
       }

        /*btnSkip.setOnClickListener {
            btnSkip.isEnabled = false          // avoid double taps
            hideKeyboard()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            overridePendingTransition(0, 0)    // no animation = snappier
        }*/

        /*btnSkip.setOnClickListener {
            btnSkip.isEnabled = false
            val t0 = android.os.SystemClock.uptimeMillis()

            // Close keyboard fast
            val ctrl = ViewCompat.getWindowInsetsController(window.decorView)
            ctrl?.hide(WindowInsetsCompat.Type.ime())
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow((currentFocus ?: window.decorView).windowToken, 0)
            currentFocus?.clearFocus()

            // Jump to Main with no animation
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
            overridePendingTransition(0, 0)
            finish()
            android.util.Log.d(
                "TRACE",
                "After startActivity, total123=${android.os.SystemClock.uptimeMillis() - t0}ms"
            )
        }*/

        btnSkip.setOnClickListener {
            val navStart = android.os.SystemClock.uptimeMillis()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

            android.util.Log.d(
                "TRACE",
                "After startActivity, total123=${android.os.SystemClock.uptimeMillis() - navStart}ms"
            )
        }

    }

    private fun View.showKeyboard() {
        requestFocus()
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun hideKeyboard() {
        currentFocus?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
            v.clearFocus()
        }
    }

    override fun onResume() {
        super.onResume()
        if (AllScreenTracker.getInstance().isManualAppForeground) {
            SetAnalytics.getInstance()
                .setActivityEvent(
                    "LoginActivity Foreground Manual", // screenName
                    this@LoginActivity            // screenClass
                )
        }
    }

}