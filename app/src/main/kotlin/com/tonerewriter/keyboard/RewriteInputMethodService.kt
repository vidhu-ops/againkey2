package com.tonerewriter.keyboard

import android.inputmethodservice.InputMethodService
import android.inputmethodservice.Keyboard
import android.inputmethodservice.KeyboardView
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import java.util.concurrent.Executors

class RewriteInputMethodService : InputMethodService(), KeyboardView.OnKeyboardActionListener {

    private lateinit var keyboardView: KeyboardView
    private lateinit var qwertyKeyboard: Keyboard
    private lateinit var symbolsKeyboard: Keyboard
    private lateinit var toneRow: LinearLayout
    private lateinit var rewriteButton: Button
    private lateinit var statusText: TextView

    private var capsLock = false
    private var isSymbols = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    private val tones = listOf("Professional", "Friendly", "Concise", "Persuasive", "Casual", "Confident")

    override fun onCreateInputView(): View {
        val root = layoutInflater.inflate(R.layout.keyboard_container, null) as LinearLayout

        keyboardView = root.findViewById(R.id.keyboardView)
        toneRow = root.findViewById(R.id.toneRow)
        rewriteButton = root.findViewById(R.id.rewriteButton)
        statusText = root.findViewById(R.id.statusText)

        qwertyKeyboard = Keyboard(this, R.xml.qwerty)
        symbolsKeyboard = Keyboard(this, R.xml.symbols)
        keyboardView.keyboard = qwertyKeyboard
        keyboardView.setOnKeyboardActionListener(this)

        buildToneChips()
        rewriteButton.setOnClickListener { onRewriteTapped() }

        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        toneRow.visibility = View.GONE
        statusText.visibility = View.GONE
        isSymbols = false
        keyboardView.keyboard = qwertyKeyboard
    }

    private fun buildToneChips() {
        toneRow.removeAllViews()
        for (tone in tones) {
            val b = Button(this)
            b.text = tone
            b.textSize = 12f
            b.setPadding(28, 10, 28, 10)
            b.setBackgroundResource(R.drawable.key_bg)
            b.setTextColor(0xFFFFFFFF.toInt())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            lp.marginEnd = 10
            b.layoutParams = lp
            b.setOnClickListener { runRewrite(tone) }
            toneRow.addView(b)
        }
    }

    private fun onRewriteTapped() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (selected.isNullOrEmpty()) {
            Toast.makeText(this, "Select some text first, then tap Rewrite", Toast.LENGTH_SHORT).show()
            return
        }
        toneRow.visibility = if (toneRow.visibility == View.VISIBLE) View.GONE else View.VISIBLE
    }

    private fun runRewrite(tone: String) {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)?.toString()
        if (selected.isNullOrEmpty()) {
            Toast.makeText(this, "Select some text first, then tap Rewrite", Toast.LENGTH_SHORT).show()
            return
        }

        statusText.text = "Rewriting…"
        statusText.visibility = View.VISIBLE

        val prefs = Prefs(this)
        val provider = prefs.provider
        val apiKey = prefs.apiKey
        val model = prefs.model

        executor.execute {
            try {
                val result = RewriteClient.rewrite(selected, tone, provider, apiKey, model)
                mainHandler.post {
                    ic.commitText(result, 1)
                    statusText.visibility = View.GONE
                    toneRow.visibility = View.GONE
                }
            } catch (e: Exception) {
                mainHandler.post {
                    statusText.visibility = View.GONE
                    Toast.makeText(
                        this@RewriteInputMethodService,
                        e.message ?: "Rewrite failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // --- Standard key handling ---

    override fun onKey(primaryCode: Int, keyCodes: IntArray?) {
        val ic = currentInputConnection ?: return
        when (primaryCode) {
            Keyboard.KEYCODE_DELETE -> {
                val selected = ic.getSelectedText(0)
                if (!selected.isNullOrEmpty()) {
                    ic.commitText("", 1)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            Keyboard.KEYCODE_SHIFT -> {
                capsLock = !capsLock
                qwertyKeyboard.isShifted = capsLock
                keyboardView.invalidateAllKeys()
            }
            Keyboard.KEYCODE_MODE_CHANGE -> {
                isSymbols = !isSymbols
                keyboardView.keyboard = if (isSymbols) symbolsKeyboard else qwertyKeyboard
            }
            10 -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            32 -> ic.commitText(" ", 1)
            else -> {
                var code = primaryCode.toChar()
                if (Character.isLetter(code) && capsLock) code = Character.toUpperCase(code)
                ic.commitText(code.toString(), 1)
            }
        }
    }

    override fun onPress(primaryCode: Int) {}
    override fun onRelease(primaryCode: Int) {}
    override fun onText(text: CharSequence?) {}
    override fun swipeLeft() {}
    override fun swipeRight() {}
    override fun swipeDown() {}
    override fun swipeUp() {}
}
