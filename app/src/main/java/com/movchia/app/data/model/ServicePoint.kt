package com.movchia.app.data.model

import kotlinx.serialization.Serializable

/**
 * Sede o punto de atención de la Secretaría de Movilidad de Chía.
 * Colección Firestore: `service_points`. Se usa en "Sedes y Puntos" y en
 * "Puntos de Atención" del modo Adultos Mayores.
 */
@Serializable
data class ServicePoint(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val phone: String = "",
    val schedule: String = "",
    val email: String = "",
    /** Enlace a la ficha de Google Maps (maps.app.goo.gl). Si existe, "Cómo llegar" lo usa. */
    val mapsUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)
