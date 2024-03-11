package com.timmy.codelab.encryption

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DeviceIdImpl(
    private val application: Application,
    private val installation: Installation,
) {

    companion object {
        private const val FILE_NAME = "prefs_device_id"

        private const val DEVICE_ID = "device_id"
        private const val LAST_DEVICE_ID = "last_device_id"
    }

    private val prefs = application.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)!!

    suspend fun getId(): String {
        val adId = withContext(Dispatchers.IO) {
            AdvertisingIdClient.getAdvertisingIdInfo(application).id
        }.takeIf { it != null && it != "00000000-0000-0000-0000-000000000000" }

        Log.i("TAG", "TimmmmmmY ====================")
        Log.i("TAG", "TimmmmmmY adid => ${adId}")

        val newDeviceId = adId ?: installation.id

        Log.i("TAG", "TimmmmmmY newDeviceId => ${newDeviceId}")

        val lastDeviceId = prefs.getString(LAST_DEVICE_ID, null)
        val savedDeviceId = prefs.getString(DEVICE_ID, null)
        Log.i("TAG", "TimmmmmmY lastDeviceId => ${lastDeviceId}")
        Log.i("TAG", "TimmmmmmY savedDeviceId => ${savedDeviceId}")

        when {
            savedDeviceId == null -> {
                Log.i("TAG", "TimmmmmmY save 沒有")
                prefs.edit {
                    putString(DEVICE_ID, newDeviceId)
                    putString(LAST_DEVICE_ID, newDeviceId)
                }
            }

            lastDeviceId != newDeviceId -> {
                Log.i("TAG", "TimmmmmmY last 跟 new 不一樣")
                prefs.edit {
                    putString(LAST_DEVICE_ID, newDeviceId)
                }
            }
        }
        return savedDeviceId ?: newDeviceId
    }
}