package com.twinalyze.servicedemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {


    private lateinit var consentInformation: ConsentInformation


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // get prefs
        val prefs = getSharedPreferences("my_prefs", MODE_PRIVATE)
        prefs.edit().putInt("cart_count", 0).apply()
        prefs.edit().clear().apply()

        lifecycleScope.launch {
            RestaurantRepository.preload(applicationContext)
        }

        consentInformation = UserMessagingPlatform.getConsentInformation(this)

        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            this,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                    this
                ) { formError ->
                    // Consent form closed or failed
                    if (formError != null) {
                        next()
                    }

                    if (consentInformation.canRequestAds()) {
                        loadAds()
                    }
                }
            },
            { requestError ->
                next()
            }
        )
    }


    private fun loadAds() {
        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(this@SplashActivity) {
                next()
            }
        }
    }


    private fun next(){
        startActivity(Intent(this@SplashActivity, StartActivity::class.java))
    }


}