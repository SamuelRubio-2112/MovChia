package com.movchia.app.navigation

import com.movchia.app.data.model.AgeGroup
import kotlinx.serialization.Serializable

/**
 * Rutas tipadas de Navigation Compose. Cada objeto/clase serializable es un
 * destino; los parámetros del constructor se convierten en argumentos.
 */
sealed interface Screen {

    @Serializable
    data object Splash : Screen

    @Serializable
    data object AgeSelection : Screen

    @Serializable
    data object KidsHome : Screen

    @Serializable
    data object AdultsHome : Screen

    @Serializable
    data object SeniorsHome : Screen

    /**
     * Manual de Tránsito + Asistente IA.
     * @param simplified muestra la versión en lenguaje claro (Adultos Mayores).
     * @param initialTab 0 = Manual, 1 = Asistente IA.
     */
    @Serializable
    data class TrafficCode(val simplified: Boolean = false, val initialTab: Int = 0) : Screen

    /**
     * Listado de contenido de un tipo (noticias, videos, cuentos, FAQ) para el
     * grupo de edad activo.
     * @param type nombre de [com.movchia.app.data.model.ContentType].
     * @param title título de la pantalla.
     */
    @Serializable
    data class ContentList(val type: String, val title: String) : Screen

    /** Visor de una pieza de contenido (texto, imagen o video). */
    @Serializable
    data class ContentDetail(val id: String) : Screen

    /**
     * Quiz reutilizable para el grupo de edad activo: "Adivina la Señal"
     * (Niños) o "Quiz de Tránsito" (Jóvenes y Adultos).
     */
    @Serializable
    data object Quiz : Screen

    /** Listado de sedes y puntos de atención. */
    @Serializable
    data class ServicePoints(val title: String = "Sedes y Puntos") : Screen

    /** Detalle de una sede con "Cómo llegar". */
    @Serializable
    data class ServicePointDetail(val id: String) : Screen

    /** Centro de notificaciones (por ahora solo vista con datos de muestra). */
    @Serializable
    data object Notifications : Screen

    /** Foros de opinión ("Opiniones"). */
    @Serializable
    data object Forums : Screen

    @Serializable
    data class ForumDetail(val id: String, val title: String) : Screen

    @Serializable
    data object AdminLogin : Screen

    @Serializable
    data object AdminDashboard : Screen

    /** Editor de contenido del CMS. [id] vacío = crear uno nuevo. */
    @Serializable
    data class AdminContentEditor(val id: String = "") : Screen

    /** Moderación de los comentarios de un foro. */
    @Serializable
    data class AdminForumPosts(val forumId: String, val title: String) : Screen
}

/** Pantalla de inicio que corresponde a cada grupo de edad. */
fun AgeGroup.homeScreen(): Screen = when (this) {
    AgeGroup.KIDS -> Screen.KidsHome
    AgeGroup.ADULTS -> Screen.AdultsHome
    AgeGroup.SENIORS -> Screen.SeniorsHome
}
