package com.timmy.codelab.encryption

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var savePasswordButton: Button
    private lateinit var getPasswordButton: Button
    private lateinit var inputEditText: EditText
    private lateinit var encryptButton: Button
    private lateinit var decryptButton: Button
    private lateinit var clearButton: Button
    private lateinit var encryptResultTextView: TextView
    private lateinit var decryptResultTextView: TextView

    private lateinit var encryptionHelper: EncryptionHelper
    private lateinit var biometricPrefsImpl: BiometricPrefsImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        encryptionHelper = EncryptionHelper(this)
        biometricPrefsImpl = BiometricPrefsImpl(this, encryptionHelper)
        setupViews()
    }

    private fun setupViews() {
        savePasswordButton = findViewById<Button>(R.id.savePasswordButton).apply {
            setOnClickListener {
                biometricPrefsImpl.password = "Timmy"
            }
        }
        getPasswordButton = findViewById<Button>(R.id.getPasswordButton).apply {
            setOnClickListener {
                Log.i("TAG", "TimmmmmmY => ${biometricPrefsImpl.password}")
            }
        }

        inputEditText = findViewById(R.id.inputEditText)
        encryptButton = findViewById<Button>(R.id.encryptButton).apply {
            setOnClickListener {
                encryptResultTextView.text = encryptionHelper.encrypt(inputEditText.text.toString())
            }
        }
        decryptButton = findViewById<Button>(R.id.decryptButton).apply {
            setOnClickListener {
                decryptResultTextView.text =
                    encryptionHelper.decrypt(encryptResultTextView.text.toString())
            }
        }
        clearButton = findViewById<Button>(R.id.clearButton).apply {
            setOnClickListener {
                inputEditText.text.clear()
                encryptResultTextView.text = ""
                decryptResultTextView.text = ""
            }
        }
        encryptResultTextView = findViewById(R.id.encryptResultTextView)
        decryptResultTextView = findViewById(R.id.decryptResultTextView)

    }
}