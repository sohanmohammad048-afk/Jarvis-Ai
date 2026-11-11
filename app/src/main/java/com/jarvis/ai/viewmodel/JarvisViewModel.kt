package com.jarvis.ai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jarvis.ai.brain.CommandProcessor
import com.jarvis.ai.control.JarvisAccessibilityService
import com.jarvis.ai.data.JarvisDatabase
import com.jarvis.ai.data.MessageEntity
import com.jarvis.ai.data.PrefEntity
import com.jarvis.ai.voice.VoiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class JarvisViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = JarvisDatabase.build(app).dao()
    val messages = dao.recentMessages().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val status = MutableStateFlow("Idle")   // Idle, Listening, Speaking, STOPPED
    val awake = MutableStateFlow(false)     // true after "Hey Jarvis"
    val bangla = MutableStateFlow(false)    // recognise Bangla instead of English

    private var pending: String? = null     // action waiting for yes/no
    private val commands = CommandProcessor(app)
    private val voice = VoiceManager(app, ::onHeard, ::onSpoken, ::onFailed)

    private val wakeWords = listOf("hey jarvis", "jarvis", "হে জার্ভিস", "জার্ভিস")
    private val sensitiveWords = listOf("send", "delete", "pay", "transfer", "uninstall")

    fun setBangla(on: Boolean) { bangla.value = on }

    fun listen() {
        if (JarvisAccessibilityService.stopped) return
        status.value = "Listening"
        voice.listen(if (bangla.value) "bn-BD" else "en-US")
    }

    fun emergencyStop() {
        JarvisAccessibilityService.stopped = true
        pending = null
        awake.value = false
        voice.stopSpeaking()
        voice.cancelListening()
        status.value = "STOPPED"
    }

    fun resume() {
        JarvisAccessibilityService.stopped = false
        status.value = "Idle"
        listen()
    }

    private fun onHeard(text: String) { viewModelScope.launch { process(text) } }

    private fun onSpoken() {
        viewModelScope.launch { if (!JarvisAccessibilityService.stopped) listen() }   // continuous mode
    }

    private fun onFailed() {
        viewModelScope.launch { delay(800); if (!JarvisAccessibilityService.stopped) listen() }
    }

    private suspend fun process(input: String) {
        if (JarvisAccessibilityService.stopped) return
        val t = input.lowercase().trim()
        dao.addMessage(MessageEntity(isUser = true, text = input))

        // 1. Waiting for yes/no on a sensitive action
        pending?.let { action ->
            pending = null
            if (listOf("yes", "haan", "হ্যাঁ").any { t.contains(it) }) say(commands.run(action) ?: "Done.")
            else say("Cancelled.")
            return
        }

        // 2. Wake word gate
        val wake = wakeWords.firstOrNull { t.contains(it) }
        if (!awake.value) {
            if (wake == null) { listen(); return }   // ignore speech until the wake word
            awake.value = true
        }

        // 3. Handle the command without the wake word
        val body = if (wake != null) t.replace(wake, "").trim() else t
        if (body.isEmpty()) { say("Yes, I'm listening."); return }

        if (body.startsWith("remember ")) {
            dao.setPref(PrefEntity("note_${System.currentTimeMillis()}", body.removePrefix("remember ")))
            say("Saved to memory.")
            return
        }

        if (sensitiveWords.any { body.contains(it) }) {
            pending = body
            say("Please confirm. Should I do this: $body? Say yes or no.")
            return
        }

        say(commands.run(body) ?: "I can't do that offline yet. An AI key in Settings will enable smart replies.")
    }

    private suspend fun say(reply: String) {
        dao.addMessage(MessageEntity(isUser = false, text = reply))
        status.value = "Speaking"
        voice.speak(reply)
    }

    override fun onCleared() { voice.shutdown() }
}
