package com.twinalyze.servicedemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.twinalyze.servicedemo.ads.Admob_InterstitialAd

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

        findViewById<Button>(R.id.btn_start).setOnClickListener {

            Admob_InterstitialAd.loadInter(this,null,object : Admob_InterstitialAd.AdEvent{
                override fun closeOnAd() {

                    Admob_InterstitialAd.show(this@StartActivity,null,object : Admob_InterstitialAd.AdEvent{
                        override fun closeOnAd() {

                            startActivity(Intent(this@StartActivity, LoginActivity::class.java))

                        }
                    })

                }
            })

        }
    }

}