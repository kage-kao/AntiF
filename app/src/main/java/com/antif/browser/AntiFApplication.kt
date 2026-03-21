package com.antif.browser

import android.app.Application
import com.antif.browser.data.AppDatabase

class AntiFApplication : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.getInstance(this)
    }

    companion object {
        lateinit var instance: AntiFApplication
            private set
    }
}
