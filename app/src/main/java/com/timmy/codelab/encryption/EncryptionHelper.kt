package com.timmy.codelab.encryption

import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.math.BigInteger
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal

class EncryptionHelper(context: Context) {

    companion object {
        private const val KEYSTORE_ALIAS = "AndroidSecure"
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val AES_MODE = "AES/GCM/NoPadding"
        private const val RSA_MODE = "RSA/ECB/PKCS1Padding"

        private const val PREF_AES_KEY = "aes_key"
        private const val PREF_AES_IV = "aes_iv"
        private const val PREFS_NAME = "prefs_encryption"
    }

    enum class Mode {
        AES, RSA
    }

    private val appContext = context.applicationContext
    private var keyStore: KeyStore? = null
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)!!

    init {
        try {
            keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
                load(null)
            }
            if (keyStore?.containsAlias(KEYSTORE_ALIAS) == false) {
                generateRsaKey()
                generateAesKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            keyStore = null
        }
    }

    @Throws(Exception::class)
    private fun generateRsaKey() {
        val certificateSubject = X500Principal("CN=Android, O=Timmy, C=TW, L=Taipei")
        val certificateSerialNumber = BigInteger.TEN
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            KeyPairGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_RSA, KEYSTORE_PROVIDER).apply {
                    initialize(
                        KeyGenParameterSpec
                            .Builder(
                                KEYSTORE_ALIAS,
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                            )
                            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                            .setCertificateSubject(certificateSubject)
                            .setCertificateSerialNumber(certificateSerialNumber)
                            .build()
                    )
                    generateKeyPair()
                }
        } else {
            val start = Calendar.getInstance()
            val end = Calendar.getInstance()
            end.add(Calendar.YEAR, 100)

            val spec = KeyPairGeneratorSpec.Builder(appContext)
                .setAlias(KEYSTORE_ALIAS)
                .setSubject(certificateSubject)
                .setSerialNumber(certificateSerialNumber)
                .setStartDate(start.time)
                .setEndDate(end.time)
                .build()
            KeyPairGenerator.getInstance("RSA", KEYSTORE_PROVIDER).apply {
                initialize(spec)
                generateKeyPair()
            }
        }
    }

    @Throws(Exception::class)
    private fun generateAesKey() {
        val secureRandom = SecureRandom()

        val aesKey = ByteArray(16)
            .also { secureRandom.nextBytes(it) }
            .let { Base64.encodeToString(it, Base64.DEFAULT) }
            .let { encrypt(it, Mode.RSA) }

        val aesIv = secureRandom.generateSeed(12)
            .let { Base64.encodeToString(it, Base64.DEFAULT) }
            .let { encrypt(it, Mode.RSA) }

        prefs.edit {
            putString(PREF_AES_KEY, aesKey)
            putString(PREF_AES_IV, aesIv)
        }
    }

    fun encrypt(string: String) = encrypt(string, Mode.AES)

    private fun encrypt(string: String, mode: Mode): String {
        if (string.isEmpty()) return string
        return try {
            when (mode) {
                Mode.AES -> getAesCipher(isEncrypt = true)
                Mode.RSA -> getRsaCipher(isEncrypt = true)
            }?.doFinal(string.toByteArray(Charsets.UTF_8))
                ?.let { Base64.encodeToString(it, Base64.DEFAULT) }
                ?: string
        } catch (e: Exception) {
            e.printStackTrace()
            string
        }
    }

    fun decrypt(string: String) = decrypt(string, Mode.AES)

    private fun decrypt(string: String, mode: Mode): String {
        if (string.isEmpty()) return string
        return try {
            when (mode) {
                Mode.AES -> getAesCipher(isEncrypt = false)
                Mode.RSA -> getRsaCipher(isEncrypt = false)
            }?.doFinal(Base64.decode(string, Base64.DEFAULT))?.toString(Charsets.UTF_8)
                ?: string
        } catch (e: Exception) {
            e.printStackTrace()
            string
        }
    }

    @Throws(Exception::class)
    private fun getRsaCipher(isEncrypt: Boolean): Cipher? {
        val keyStore = keyStore ?: return null
        val mode = if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        val key = if (isEncrypt) {
            keyStore.getCertificate(KEYSTORE_ALIAS).publicKey
        } else {
            keyStore.getKey(KEYSTORE_ALIAS, null) as PrivateKey
        }
        return Cipher.getInstance(RSA_MODE).apply {
            init(mode, key)
        }
    }

    @Throws(Exception::class)
    private fun getAesCipher(isEncrypt: Boolean): Cipher? {
        val aesKey = prefs.getString(PREF_AES_KEY, null)?.let {
            Base64.decode(decrypt(it, Mode.RSA), Base64.DEFAULT)
        } ?: return null

        val aesIv = prefs.getString(PREF_AES_IV, null)?.let {
            Base64.decode(decrypt(it, Mode.RSA), Base64.DEFAULT)
        } ?: return null

        val mode = if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE
        return Cipher.getInstance(AES_MODE).apply {
            init(mode, SecretKeySpec(aesKey, AES_MODE), IvParameterSpec(aesIv))
        }
    }
}