package com.tonerewriter.keyboard

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("tone_rewriter_prefs", Context.MODE_PRIVATE)

    var provider: String
        get() = sp.getString("provider", "groq") ?: "groq"
        set(value) = sp.edit().putString("provider", value).apply()

    var apiKey: String
        get() = sp.getString("api_key", "") ?: ""
        set(value) = sp.edit().putString("api_key", value).apply()

    var model: String
        get() = sp.getString("model", "") ?: ""
        set(value) = sp.edit().putString("model", value).apply()
}
