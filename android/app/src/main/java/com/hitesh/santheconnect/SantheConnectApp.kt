package com.hitesh.santheconnect

import android.app.Application
import com.google.firebase.FirebaseApp

class SantheConnectApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
