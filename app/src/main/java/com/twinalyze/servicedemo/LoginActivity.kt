package com.twinalyze.servicedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.twinalyze.servicedemo.ads.Admob_InterstitialAd

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

        btnSkip.setOnClickListener {

            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

    }

    private fun View.showKeyboard() {
        requestFocus()
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(this, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
    }

}