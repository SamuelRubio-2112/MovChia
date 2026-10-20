package com.movchia.app.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Envoltorio de [TextToSpeech] para el modo Adultos Mayores (botón "Escuchar").
 * Inicializa el motor una sola vez, prefiere español de Colombia y hace cola
 * con lo que se pida leer antes de que el motor esté listo.
 */
class TextToSpeechHelper(context: Context) {

    private var tts: TextToSpeech? = null
    private var pendingText: String? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                _isReady.value = true
                pendingText?.let { speak(it) }
                pendingText = null
            }
        }
    }

    private fun configureLanguage() {
        val engine = tts ?: return
        val colombia = Locale.forLanguageTag("es-CO")
        val result = engine.setLanguage(colombia)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.forLanguageTag("es"))
        }
        engine.setSpeechRate(0.9f) // Ligeramente más lento para mayor claridad.
    }

    /** Lee [text] en voz alta, interrumpiendo cualquier lectura en curso. */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (!_isReady.value) {
            pendingText = text
            return
        }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "movchia_${text.hashCode()}")
    }

    fun stop() {
        tts?.stop()
    }

    /** Libera el motor. Llamar cuando la app se cierre definitivamente. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
    }
}
