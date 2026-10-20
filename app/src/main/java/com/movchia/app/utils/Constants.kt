package com.movchia.app.utils

/** Constantes globales de la aplicación. */
object Constants {
    const val APP_NAME = "MovChia"
    const val INSTITUTION = "Secretaría de Movilidad de Chía"

    /** Línea nacional de emergencias mostrada en el modo Adultos Mayores. */
    const val EMERGENCY_NUMBER = "123"

    /** Norma base del Manual de Tránsito y del Asistente de IA. */
    const val TRAFFIC_CODE_LAW = "Ley 769 de 2002 - Código Nacional de Tránsito Terrestre"

    /** Endpoint del asistente de IA. Placeholder: configurar cuando exista el backend. */
    const val AI_ASSISTANT_ENDPOINT = "https://REEMPLAZAR.cloudfunctions.net/trafficAssistant"

    /** Duración del splash antes de navegar (ms). */
    const val SPLASH_DURATION_MS = 1500L

    /** Altura mínima de los botones accesibles del modo Adultos Mayores (dp). */
    const val SENIOR_BUTTON_MIN_HEIGHT_DP = 72

    /** Factor de escala tipográfica del modo Adultos Mayores. */
    const val SENIOR_FONT_SCALE = 1.25f
}
