package com.movchia.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.movchia.app.data.model.AgeGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Puntos acumulados en los quizzes, guardados localmente por grupo de edad.
 * No se sincronizan con Firebase a propósito: evita escrituras en Firestore
 * y funciona sin conexión. El Home de Niños muestra el total de [AgeGroup.KIDS].
 */
class PointsLocalStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val flows = mutableMapOf<AgeGroup, MutableStateFlow<Int>>()

    private fun flowFor(group: AgeGroup): MutableStateFlow<Int> =
        flows.getOrPut(group) { MutableStateFlow(prefs.getInt(keyFor(group), 0)) }

    /** Total acumulado del grupo; emite cada vez que se suman puntos. */
    fun points(group: AgeGroup): StateFlow<Int> = flowFor(group).asStateFlow()

    fun add(group: AgeGroup, points: Int) {
        if (points <= 0) return
        val flow = flowFor(group)
        val total = flow.value + points
        prefs.edit().putInt(keyFor(group), total).apply()
        flow.value = total
    }

    private fun keyFor(group: AgeGroup) = "$KEY_PREFIX${group.storageKey}"

    private companion object {
        const val PREFS_NAME = "movchia_prefs"
        const val KEY_PREFIX = "quiz_points_"
    }
}
