package com.timmy.codelab.encryption

import android.app.Application
import android.util.Log
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceIdImpl(
    private val application: Application,
    private val installation: Installation,
) {
    suspend fun getId(): String {
        val adId = withContext(Dispatchers.IO) {
            AdvertisingIdClient.getAdvertisingIdInfo(application).id
        }.takeIf {
            it != null && it != "00000000-0000-0000-0000-000000000000"
        }
        Log.i("TAG", "TimmmmmmY adid => ${adId}")
        val deviceId = adId ?: installation.id
        Log.i("TAG", "TimmmmmmY deviceId => ${deviceId}")
        return deviceId
    }
}