package com.timmy.codelab.encryption

import android.app.Application
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*

class Installation(private val application: Application) {

    companion object {
        private const val INSTALLATION = "INSTALLATION"
    }

    private var sID: String? = null
    @get:Synchronized
    val id: String
        get() {
            if (sID == null) {
                val installation = File(application.filesDir, INSTALLATION)
                try {
                    if (!installation.exists()) {
                        writeInstallationFile(installation)
                    }
                    sID = readInstallationFile(installation)
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
            return sID!!
        }

    @Throws(IOException::class)
    private fun readInstallationFile(installation: File): String {
        val f = RandomAccessFile(installation, "r")
        val bytes = ByteArray(f.length().toInt())
        f.readFully(bytes)
        f.close()
        return String(bytes)
    }

    @Throws(IOException::class)
    private fun writeInstallationFile(installation: File) {
        val out = FileOutputStream(installation)
        val id = UUID.randomUUID().toString()
        out.write(id.toByteArray())
        out.close()
    }
}