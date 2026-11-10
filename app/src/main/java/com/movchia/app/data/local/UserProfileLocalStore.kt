package com.movchia.app.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Apodo con el que la persona firma sus comentarios en los foros. Se guarda
 * solo en el teléfono; no hay cuenta ni datos personales.
 */
class UserProfileLocalStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _nickname = MutableStateFlow(prefs.getString(KEY_NICKNAME, "").orEmpty())

    /** Cadena vacía mientras la persona no haya elegido apodo. */
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    fun saveNickname(value: String) {
        val clean = sanitizeNickname(value)
        prefs.edit().putString(KEY_NICKNAME, clean).apply()
        _nickname.value = clean
    }

    companion object {
        private const val PREFS_NAME = "movchia_prefs"
        private const val KEY_NICKNAME = "forum_nickname"
        const val NICKNAME_MIN = 3
        const val NICKNAME_MAX = 20

        /** Recorta espacios repetidos y limita la longitud. */
        fun sanitizeNickname(raw: String): String =
            raw.trim().replace(Regex("\\s+"), " ").take(NICKNAME_MAX)

        fun isValidNickname(raw: String): Boolean = sanitizeNickname(raw).length >= NICKNAME_MIN
    }
}
