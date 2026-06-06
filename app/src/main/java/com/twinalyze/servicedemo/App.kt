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
            "e50bd15599130493594306921558a47d2a39c754f5",
            "496d043f2b53dfd2acc1de118f26f591a0b52a9943b6e248a817b6a5d776a184",
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