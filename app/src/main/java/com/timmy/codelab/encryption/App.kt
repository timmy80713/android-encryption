package com.timmy.codelab.encryption

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val installation = Installation(this)
        val deviceIdImpl = DeviceIdImpl(this, installation)
        GlobalScope.launch {
            deviceIdImpl.getId()
        }
    }
}