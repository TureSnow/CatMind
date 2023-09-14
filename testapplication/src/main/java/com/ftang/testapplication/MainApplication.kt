package com.ftang.testapplication

import android.app.Application
import com.ftang.catmind.CatMind

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CatMind.initialize(this)
    }
}