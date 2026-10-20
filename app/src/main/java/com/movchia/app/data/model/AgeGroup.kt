package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/**
 * Rango de edad del usuario. Define la experiencia completa de la app:
 * pantalla de inicio, tema visual, tipografía y accesibilidad.
 *
 * - KIDS: 0 a 12 años. Experiencia lúdica (cuentos, dibujos, juegos).
 * - ADULTS: 13 a 59 años. Experiencia informativa (noticias, trámites, campañas).
 * - SENIORS: 60 años o más. Alto contraste, botones gigantes y lectura por voz.
 */
@Serializable
enum class AgeGroup(
    val minAge: Int,
    val maxAge: Int,
    val displayName: String,
    val storageKey: String,
) {
    KIDS(minAge = 0, maxAge = 12, displayName = "Niños", storageKey = "kids"),
    ADULTS(minAge = 13, maxAge = 59, displayName = "Jóvenes y Adultos", storageKey = "adults"),
    SENIORS(minAge = 60, maxAge = Int.MAX_VALUE, displayName = "Adultos Mayores", storageKey = "seniors");

    companion object {
        fun fromAge(age: Int): AgeGroup =
            entries.firstOrNull { age in it.minAge..it.maxAge } ?: ADULTS

        fun fromStorageKey(key: String?): AgeGroup? =
            entries.firstOrNull { it.storageKey == key }
    }
}
