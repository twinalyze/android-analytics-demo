package com.twinalyze.servicedemo

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.twinalyze.Twinalyze
import com.twinalyze.listeners.TwinalyzeInitializationStatus
import com.twinalyze.model.TwinalyzeError

class App : Application() {


    override fun onCreate() {
        super.onCreate()

        Twinalyze.initialize(this,
            "6e1fa2b44a5364988627d95280e5a8992112acad8a",
            "3d4eac131f145649063432b4e7b062254ded17e0bf530da61dd9c564f387f332",
            object : TwinalyzeInitializationStatus {
                override fun onSuccess() {
                    Log.d("App@@@", "onSuccess")
                }

                override fun onFailed(twinalyzeError: TwinalyzeError) {
                    Log.d("App@@@", ""+twinalyzeError.errorCode + " "+twinalyzeError.errorMessage)
                    Toast.makeText(this@App, ""+twinalyzeError.errorCode + " "+twinalyzeError.errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
    }

}