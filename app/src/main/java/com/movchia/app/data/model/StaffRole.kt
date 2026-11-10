package com.movchia.app.data.model

/**
 * Rol del personal de la Secretaría en el panel de gestión. Se guarda en
 * Firestore en `users/{uid}.role` como texto en minúsculas ("gestor"/"root");
 * las reglas de seguridad usan ese mismo valor.
 */
enum class StaffRole(val firestoreValue: String, val displayName: String) {
    /** Publica y edita contenido, administra sedes y modera los foros. */
    GESTOR("gestor", "Gestor de contenidos"),

    /** Todo lo del gestor + administración de usuarios y analítica. */
    ROOT("root", "Administrador");

    companion object {
        fun fromFirestore(value: String?): StaffRole? =
            entries.firstOrNull { it.firestoreValue == value?.trim()?.lowercase() }
    }
}
