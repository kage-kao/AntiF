package com.antif.browser

import android.app.Application
import com.antif.browser.data.AppDatabase
import com.antif.browser.data.BypassSettings

class AntiFApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize database
        database = AppDatabase.getInstance(this)
        
        // Initialize BypassSettings EARLY to prevent crashes
        BypassSettings.init(this)
    }

    companion object {
        lateinit var instance: AntiFApplication
            private set
    }
}
