package com.twinalyze.servicedemo

import android.app.Application
import com.twinalyze.event.Analytics


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        //Twinalyze Analytics SDK Initializing (this, apiKey, organization id, true)
        Analytics.getInstance().initialize(
            this,
            "kh2RwGFsqNr1gh1NWWExAewbvIzYyNk",
            "twinalyze_demo_7356297484",
            true
        )

    }

}