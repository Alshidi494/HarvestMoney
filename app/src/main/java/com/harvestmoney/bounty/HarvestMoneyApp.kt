package com.harvestmoney.bounty

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.FirebaseApp

class HarvestMoneyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize AdMob
        MobileAds.initialize(this)
        
        // Set AdMob configuration for test devices and child-directed treatment
        val configuration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf()) // Add your test device IDs here
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(configuration)
    }
}
