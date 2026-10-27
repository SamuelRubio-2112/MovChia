package com.movchia.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Envoltorio de [TextToSpeech] para el modo Adultos Mayores (botón "Escuchar").
 * Inicializa el motor una sola vez, prefiere español de Colombia y hace cola
 * con lo que se pida leer antes de que el motor esté listo.
 *
 * Compatibilidad: en equipos antiguos (p. ej. Android 8) la voz en español
 * suele no venir instalada y el motor la sintetiza por red. En lugar de
 * condicionar por versión de Android, se consulta la capacidad real de la voz
 * y, si no se puede leer, se publica un aviso en [problem] para la UI.
 */
class TextToSpeechHelper(context: Context) {

    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var pendingText: String? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _problem = MutableStateFlow<String?>(null)

    /** Mensaje para el usuario cuando la lectura en voz no es posible; `null` si todo va bien. */
    val problem: StateFlow<String?> = _problem.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                configureLanguage()
                _isReady.value = true
                pendingText?.let { speak(it) }
                pendingText = null
            } else {
                _problem.value = MSG_NO_ENGINE
            }
        }.apply {
            setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _problem.value = null
                }

                override fun onDone(utteranceId: String?) = Unit

                @Deprecated("Requerido por la clase base")
                override fun onError(utteranceId: String?) {
                    _problem.value = MSG_FAILED
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w(TAG, "Error de síntesis $errorCode")
                    _problem.value = if (errorCode == TextToSpeech.ERROR_NETWORK ||
                        errorCode == TextToSpeech.ERROR_NETWORK_TIMEOUT
                    ) MSG_NEEDS_NETWORK else MSG_FAILED
                }
            })
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

    /** `true` si la voz activa no está instalada en el equipo y depende de la red. */
    private fun voiceNeedsNetwork(engine: TextToSpeech): Boolean {
        val voice = engine.voice ?: return false
        Log.d(TAG, "Voz ${voice.name}, red=${voice.isNetworkConnectionRequired}, features=${voice.features}")
        return voice.isNetworkConnectionRequired ||
            voice.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED)
    }

    private fun isOnline(): Boolean {
        val cm = appContext.getSystemService(ConnectivityManager::class.java) ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /** Lee [text] en voz alta, interrumpiendo cualquier lectura en curso. */
    fun speak(text: String) {
        if (text.isBlank()) return
        val engine = tts
        if (!_isReady.value || engine == null) {
            pendingText = text
            return
        }
        if (voiceNeedsNetwork(engine) && !isOnline()) {
            _problem.value = MSG_NEEDS_NETWORK
            return
        }
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "movchia_${text.hashCode()}")
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

    private companion object {
        const val TAG = "MovChiaTTS"
        const val MSG_NEEDS_NETWORK =
            "La voz en español de este teléfono necesita internet. Conéctese o lea las instrucciones en pantalla."
        const val MSG_FAILED = "No fue posible leer en voz alta. Lea las instrucciones en pantalla."
        const val MSG_NO_ENGINE = "Este teléfono no tiene lector de voz disponible."
    }
}
