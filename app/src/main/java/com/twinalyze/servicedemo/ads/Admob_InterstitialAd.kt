package com.twinalyze.servicedemo.ads

import android.app.Activity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.twinalyze.Twinalyze
import com.twinalyze.servicedemo.util.LoaderDialog
import com.twinalyze.servicedemo.util.getActivityFullName
import com.twinalyze.utils.AdsCompany
import com.twinalyze.utils.AdsType

object Admob_InterstitialAd {

    private var interstitialAd : InterstitialAd ?= null
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    private var uniqId = ""

    private var isAdLoading = false

    var loader : LoaderDialog ?= null


    interface AdEvent{
        fun closeOnAd()
    }


    fun loadInter(activity: Activity,fragment: androidx.fragment.app.Fragment?=null,adEvent : AdEvent){

        if(isAdLoading || interstitialAd != null){
            adEvent.closeOnAd()
            return
        }

        loader = LoaderDialog(activity)
        loader?.show()
        isAdLoading = true

        uniqId = System.currentTimeMillis().toString()

        Twinalyze.setAdsRequestEvent(
            AdsCompany.Admob,
            AdsType.Interstitial,
            getActivityFullName(activity,fragment),
            AD_UNIT_ID,
            uniqId
        )

        InterstitialAd.load(
            activity,
            AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isAdLoading = false
                    loader?.dismiss()

                    Twinalyze.setAdsLoadedEvent(
                        AdsCompany.Admob,
                        AdsType.Interstitial,
                        getActivityFullName(activity,fragment),
                        AD_UNIT_ID,
                        uniqId
                    )

                    adEvent.closeOnAd()

                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAd = null

                    isAdLoading = false
                    loader?.dismiss()

                    Twinalyze.setAdFailedToLoadEvent(
                        AdsCompany.Admob,
                        AdsType.Interstitial,
                        getActivityFullName(activity,fragment),
                        AD_UNIT_ID,
                        uniqId,
                        null,
                        adError.code.toString(),
                        adError.message
                    )

                    adEvent.closeOnAd()

                }
            },
        )

    }


    fun show(activity: Activity,fragment: androidx.fragment.app.Fragment?=null,adEvent : AdEvent){

        if(interstitialAd == null || isAdLoading){
            adEvent.closeOnAd()
            return
        }

        interstitialAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    adEvent.closeOnAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    Twinalyze.setAdFailedToShowEvent(
                        AdsCompany.Admob,
                        AdsType.Interstitial,
                        getActivityFullName(activity,fragment),
                        AD_UNIT_ID,
                        uniqId,
                        null,
                        adError.code.toString(),
                        adError.message)
                    adEvent.closeOnAd()
                }

                override fun onAdShowedFullScreenContent() {
                }

                override fun onAdImpression() {
                    Twinalyze.setAdsImpressionEvent(
                        AdsCompany.Admob,
                        AdsType.Interstitial,
                        getActivityFullName(activity,fragment),
                        AD_UNIT_ID,
                        uniqId,
                        null)
                }

                override fun onAdClicked() {

                    Twinalyze.setAdsClickEvent(
                        AdsCompany.Admob,
                        AdsType.Interstitial,
                        getActivityFullName(activity,fragment),
                        AD_UNIT_ID,
                        uniqId,
                        null
                    )

                }
            }

        interstitialAd?.onPaidEventListener = OnPaidEventListener { adValue ->
            Twinalyze.setAdsPaidEvent(
                AdsCompany.Admob,
                AdsType.Interstitial,
                getActivityFullName(activity,fragment),
                AD_UNIT_ID,
                uniqId,
                null,
                adValue.currencyCode,
                adValue.precisionType.toString(),
                adValue.valueMicros.toString()
            )
        }

        interstitialAd?.show(activity)
    }
}