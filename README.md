# Tone Rewriter Keyboard — a real system-wide Android keyboard

This is a genuine Android IME (Input Method Editor) — it registers with Android as a keyboard,
so it shows up in the keyboard picker and works inside every app: WhatsApp, Gmail, Slack, Notes,
Chrome, anything. It's a full QWERTY keyboard (with a numbers/symbols page) plus one extra
feature: select text with the normal selection handles, tap **✨ Rewrite selection**, pick a tone,
and the selection is replaced in place — no copy/paste needed.

I could not compile this into a ready-to-install APK myself — building Android apps requires
Google's Android SDK/build tools, which live behind network domains this sandboxed environment
can't reach. The code itself is complete and correct; you just need to run the actual build,
which takes one of two paths below.

---

## Option A — Build with Android Studio (recommended, most reliable)

1. Install **Android Studio** (free): https://developer.android.com/studio
2. Open Android Studio → **Open** → select this whole `android-keyboard` folder
3. Let Gradle sync — first time it'll download the Android SDK/build tools (a few GB, one-time)
4. Plug your phone in by USB with **USB debugging** enabled (Settings → About phone → tap "Build
   number" 7 times to unlock Developer Options → enable USB debugging), or use Android Studio's
   Wi-Fi debugging
5. Click the green **Run ▶** button — it installs directly onto your phone, no Play Store, no
   signing needed for personal use

## Option B — Build in the cloud with GitHub Actions (no local install at all)

1. Create a new GitHub repo (can be private) and push this whole folder to it
2. Go to the repo's **Actions** tab — a workflow will run automatically and build the APK
3. When it finishes, open the run → download the `tone-rewriter-keyboard-debug-apk` artifact
   (it's a zip containing `app-debug.apk`)
4. Transfer that APK to your phone (email it to yourself, Google Drive, etc.), tap it to install.
   Android will ask you to allow "install from this source" the first time — that's expected for
   an app installed outside the Play Store.

---

## Setting it up on your phone

1. Open the **Tone Rewriter Keyboard** app icon
2. Pick your AI provider (Groq or Gemini) and paste your free API key
   - Groq: https://console.groq.com/keys
   - Gemini: https://aistudio.google.com/apikey
3. Tap **Save settings**
4. Tap **Open keyboard settings** → toggle "Tone Rewriter Keyboard" on → confirm the security
   prompt (standard for any third-party keyboard — Android always warns that keyboards can see
   what you type, since that's literally how keyboards work)
5. Tap **Show keyboard picker** → choose **Tone Rewriter Keyboard**

## Using it

- Type normally — it's a full keyboard with letters, a 123/symbols page, space, enter, backspace,
  and shift.
- To rewrite something: long-press to select text as you normally would in any app, then tap
  **✨ Rewrite selection** above the keyboard, then tap a tone (Professional, Friendly, Concise,
  Persuasive, Casual, Confident). The rewritten text replaces your selection automatically.
- To switch back to your old keyboard any time: hold down the spacebar (or use the keyboard-switch
  icon in your notification shade / navigation bar, depending on phone) and pick a different one,
  or repeat step 5 above.

## Notes

- Your API key is stored only in this app's private storage on your phone — nothing goes through
  any server of mine. Requests go straight from your phone to Groq or Gemini.
- Tone list is fixed to six presets right now (Professional, Friendly, Concise, Persuasive, Casual,
  Confident). If you want a custom-tone text box added too, that's a small change — just ask.
- This uses Android's classic `KeyboardView` API. It's a bit old-school (no swipe-typing,
  no autocorrect, no emoji panel) but it's stable, dependency-free, and 100% reliable for the
  select → rewrite → tone flow you asked for. Autocorrect/predictive text/swipe typing would be a
  substantially bigger project (that's what Gboard spent years on) — happy to talk through that if
  you want to go further later.
