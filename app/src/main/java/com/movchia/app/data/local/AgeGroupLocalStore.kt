package com.movchia.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.movchia.app.data.model.AgeGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Persistencia local del rango de edad seleccionado por el usuario.
 * Usa SharedPreferences (suficiente para un único valor) y expone un
 * [StateFlow] para que el tema y la navegación reaccionen a los cambios.
 */
class AgeGroupLocalStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _ageGroup = MutableStateFlow(
        AgeGroup.fromStorageKey(prefs.getString(KEY_AGE_GROUP, null))
    )

    /** `null` mientras el usuario no haya elegido un rango de edad. */
    val ageGroup: StateFlow<AgeGroup?> = _ageGroup.asStateFlow()

    fun save(group: AgeGroup) {
        prefs.edit().putString(KEY_AGE_GROUP, group.storageKey).apply()
        _ageGroup.value = group
    }

    fun clear() {
        prefs.edit().remove(KEY_AGE_GROUP).apply()
        _ageGroup.value = null
    }

    private companion object {
        const val PREFS_NAME = "movchia_prefs"
        const val KEY_AGE_GROUP = "age_group"
    }
}
