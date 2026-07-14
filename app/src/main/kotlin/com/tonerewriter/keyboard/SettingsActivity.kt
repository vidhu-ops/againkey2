package com.tonerewriter.keyboard

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = Prefs(this)
        val providerSpinner = findViewById<Spinner>(R.id.providerSpinner)
        val apiKeyField = findViewById<EditText>(R.id.apiKeyField)
        val modelField = findViewById<EditText>(R.id.modelField)

        val providers = arrayOf("groq", "gemini")
        providerSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, providers)
        providerSpinner.setSelection(providers.indexOf(prefs.provider).coerceAtLeast(0))
        apiKeyField.setText(prefs.apiKey)
        modelField.setText(prefs.model)

        findViewById<Button>(R.id.saveButton).setOnClickListener {
            prefs.provider = providers[providerSpinner.selectedItemPosition]
            prefs.apiKey = apiKeyField.text.toString().trim()
            prefs.model = modelField.text.toString().trim()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.enableKeyboardButton).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        findViewById<Button>(R.id.switchKeyboardButton).setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }
    }
}
