package com.jarvis.ai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/** Speech-to-text (free Android recognizer) and text-to-speech. */
class VoiceManager(
    private val appContext: Context,
    private val onTextHeard: (String) -> Unit,
    private val onSpokenDone: () -> Unit,
    private val onFailed: () -> Unit
) {
    private var ready = false
    private var recognizer: SpeechRecognizer? = null
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(appContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(id: String?) {}
                override fun onDone(id: String?) = onSpokenDone()
                override fun onError(id: String?) = onSpokenDone()
            })
        }
    }

    fun speak(text: String) {
        if (!ready) { onSpokenDone(); return }
        tts.language = if (text.any { it in '\u0980'..'\u09FF' }) Locale("bn", "BD") else Locale.US
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis-${System.currentTimeMillis()}")
    }

    fun listen(language: String) {
        recognizer?.destroy()
        val sr = SpeechRecognizer.createSpeechRecognizer(appContext)
        sr.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (text.isNullOrBlank()) onFailed() else onTextHeard(text)
            }
            override fun onError(error: Int) = onFailed()
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        recognizer = sr
        sr.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                .putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        )
    }

    fun cancelListening() { recognizer?.cancel() }
    fun stopSpeaking() { tts.stop() }
    fun shutdown() { recognizer?.destroy(); tts.shutdown() }
}
