package com.twinalyze.servicedemo

import android.app.Application
import com.twinalyze.event.Analytics


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        //Twinalyze Analytics SDK Initializing (this, apiKey, organization id, true)
        Analytics.getInstance().initialize(this,"MmlbthQ2Dc6itPDUT0dE9Gfh5hOrou5","twinalyze_demo_3498901526",true)

    }

}