package com.twinalyze.servicedemo

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.twinalyze.event.SetAnalytics
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SetAnalytics.getInstance().setSplashTimeEvent(1000)

        SetAnalytics.getInstance()
            .setActivityEvent(
                "SplashActivity Manual",    // screenName
                this@SplashActivity // screenClass
            )

        // get prefs
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        prefs.edit().putInt("cart_count", 0).apply()
        prefs.edit().clear().apply()

        val handler = Handler(Looper.getMainLooper())

        lifecycleScope.launch {
            RestaurantRepository.preload(applicationContext)

            handler.postDelayed({
                startActivity(Intent(this@SplashActivity, StartActivity::class.java))
            }, 2000)
        }
    }
}