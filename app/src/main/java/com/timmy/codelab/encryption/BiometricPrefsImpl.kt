package com.timmy.codelab.encryption

import android.content.Context
import android.util.Log
import androidx.core.content.edit

class BiometricPrefsImpl(
    context: Context,
    private val encryptionHelper: EncryptionHelper,
) {

    companion object {
        private const val FILE_NAME = "prefs_biometric"

        private const val PASSWORD = "password"
    }

    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)!!

    var password: String
        get() {
            val encryptedString = prefs.getString(PASSWORD, "")
            Log.i("TAG", "TimmmmmmY get encryptedString => ${encryptedString}")
            if (encryptedString.isNullOrBlank()) return ""
            val decryptedString = encryptionHelper.decrypt(encryptedString)
            Log.i("TAG", "TimmmmmmY get decryptedString => ${decryptedString}")
            return decryptedString
        }
        set(value) {
            Log.i("TAG", "TimmmmmmY set value => ${value}")
            val encryptedValue = encryptionHelper.encrypt(value)
            Log.i("TAG", "TimmmmmmY set encryptedValue => ${encryptedValue}")
            prefs.edit {
                putString(PASSWORD, encryptedValue)
            }
        }

    fun clear() {
        prefs.edit {
            remove(PASSWORD)
        }
    }
}