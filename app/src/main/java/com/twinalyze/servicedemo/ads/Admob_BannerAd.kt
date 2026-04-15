package com.twinalyze.servicedemo.ads

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnPaidEventListener
import com.twinalyze.Twinalyze
import com.twinalyze.servicedemo.util.getActivityFullName
import com.twinalyze.utils.AdsCompany
import com.twinalyze.utils.AdsType

class Admob_BannerAd {

    private val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"
    private var uniqId = ""
    private var bannerImpression = 0

    fun getSubUniqId() : String{
        return uniqId + "_"+bannerImpression
    }


    fun loadBanner(activity: Activity,fragment: androidx.fragment.app.Fragment?=null,viewGroup: ViewGroup){

        uniqId = System.currentTimeMillis().toString()

        val adView = AdView(activity)
        adView.adUnitId = BANNER_AD_UNIT_ID
        adView.setAdSize(AdSize.BANNER)

        Twinalyze.setAdsRequestEvent(
            AdsCompany.Admob,
            AdsType.Banner,
            getActivityFullName(activity,fragment),
            BANNER_AD_UNIT_ID,
            uniqId
        )


        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                super.onAdClicked()
                Twinalyze.setAdsClickEvent(
                    AdsCompany.Admob,
                    AdsType.Banner,
                    getActivityFullName(activity,fragment),
                    BANNER_AD_UNIT_ID,
                    uniqId,
                    getSubUniqId()
                )
            }

            override fun onAdClosed() {
                super.onAdClosed()
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)

                Twinalyze.setAdFailedToLoadEvent(
                    AdsCompany.Admob,
                    AdsType.Banner,
                    getActivityFullName(activity,fragment),
                    BANNER_AD_UNIT_ID,
                    uniqId,
                    getSubUniqId(),
                    adError.code.toString(),
                    adError.message
                )
            }

            override fun onAdImpression() {
                super.onAdImpression()
                bannerImpression++
                Twinalyze.setAdsImpressionEvent(
                    AdsCompany.Admob,
                    AdsType.Banner,
                    getActivityFullName(activity,fragment),
                    BANNER_AD_UNIT_ID,
                    uniqId,
                    getSubUniqId()
                )
            }

            override fun onAdLoaded() {
                super.onAdLoaded()
                Twinalyze.setAdsLoadedEvent(
                    AdsCompany.Admob,
                    AdsType.Banner,
                    getActivityFullName(activity,fragment),
                    BANNER_AD_UNIT_ID,
                    uniqId
                )


                viewGroup.visibility = View.VISIBLE
                viewGroup.removeAllViews()
                viewGroup.addView(adView)

            }

            override fun onAdOpened() {
                super.onAdOpened()

            }

            override fun onAdSwipeGestureClicked() {
                super.onAdSwipeGestureClicked()

            }
        }

        adView.onPaidEventListener = OnPaidEventListener { adValue ->
            Twinalyze.setAdsPaidEvent(
                AdsCompany.Admob,
                AdsType.Banner,
                getActivityFullName(activity,fragment),
                BANNER_AD_UNIT_ID,
                uniqId,
                getSubUniqId(),
                adValue.currencyCode,
                adValue.precisionType.toString(),
                adValue.valueMicros.toString()
            )
        }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }




}