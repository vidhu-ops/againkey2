package com.tonerewriter.keyboard

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * Minimal blocking HTTP client — no OkHttp/Retrofit dependency needed.
 * Must be called from a background thread (the IME calls this from an Executor).
 */
object RewriteClient {

    fun rewrite(text: String, tone: String, provider: String, apiKey: String, model: String): String {
        if (apiKey.isBlank()) {
            throw IllegalStateException("No API key set. Open the Tone Rewriter Keyboard app to add one.")
        }

        val prompt = "Rewrite the following text in a $tone tone. " +
            "Preserve the original meaning and roughly the same length. " +
            "Return ONLY the rewritten text, with no quotes, preamble, or explanation.\n\n" +
            "Text:\n$text"

        return if (provider == "gemini") {
            callGemini(prompt, apiKey, model.ifBlank { "gemini-2.0-flash" })
        } else {
            callGroq(prompt, apiKey, model.ifBlank { "llama-3.3-70b-versatile" })
        }
    }

    private fun callGroq(prompt: String, apiKey: String, model: String): String {
        val url = URL("https://api.groq.com/openai/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.doOutput = true
            conn.connectTimeout = 20000
            conn.readTimeout = 30000

            val messages = JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
            val body = JSONObject()
                .put("model", model)
                .put("messages", messages)
                .put("temperature", 0.6)

            conn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("Groq error ($code): ${responseText.take(200)}")

            val json = JSONObject(responseText)
            return json.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content").trim()
        } finally {
            conn.disconnect()
        }
    }

    private fun callGemini(prompt: String, apiKey: String, model: String): String {
        val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 20000
            conn.readTimeout = 30000

            val parts = JSONArray().put(JSONObject().put("text", prompt))
            val contents = JSONArray().put(JSONObject().put("parts", parts))
            val body = JSONObject().put("contents", contents)

            conn.outputStream.use { it.write(body.toString().toByteArray(StandardCharsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val responseText = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) throw RuntimeException("Gemini error ($code): ${responseText.take(200)}")

            val json = JSONObject(responseText)
            return json.getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content").getJSONArray("parts").getJSONObject(0)
                .getString("text").trim()
        } finally {
            conn.disconnect()
        }
    }
}
