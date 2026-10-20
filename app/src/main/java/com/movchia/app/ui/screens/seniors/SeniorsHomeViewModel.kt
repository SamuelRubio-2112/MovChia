package com.movchia.app.ui.screens.seniors

import androidx.lifecycle.ViewModel
import com.movchia.app.utils.TextToSpeechHelper

/**
 * Pantalla 06_Home_AdultosMayores. Su única lógica es la lectura por voz
 * de las instrucciones en pantalla (botón "Escuchar").
 */
class SeniorsHomeViewModel(
    private val tts: TextToSpeechHelper,
) : ViewModel() {

    val isTtsReady = tts.isReady

    fun readScreenAloud() {
        tts.speak(
            "Bienvenido a Mayores Seguros. Seleccione con un toque el botón grande para abrir la información. " +
                "Opciones disponibles: Ver manual simple. Puntos de atención. Videos explicativos. Preguntas frecuentes. " +
                "En la parte inferior está el botón rojo de emergencia uno dos tres.",
        )
    }

    fun speak(text: String) = tts.speak(text)

    override fun onCleared() {
        tts.stop()
        super.onCleared()
    }
}
