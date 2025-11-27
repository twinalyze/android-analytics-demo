package com.twinalyze.servicedemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.twinalyze.alldatget.AllScreenTracker
import com.twinalyze.event.SetAnalytics

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_start)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SetAnalytics.getInstance()
            .setActivityEvent(
                "StartActivity manual",    // screenName
                this@StartActivity // screenClass
            )

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            startActivity(Intent(this@StartActivity, LoginActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        if (AllScreenTracker.getInstance().isManualAppForeground) {
            SetAnalytics.getInstance()
                .setActivityEvent(
                    "StartActivity Foreground Manual", // screenName
                    this@StartActivity            // screenClass
                )
        }

    }
}