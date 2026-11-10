package com.movchia.app

import android.app.Application
import com.movchia.app.data.AppContainer
import com.movchia.app.data.DefaultAppContainer

/**
 * Clase Application de MovChia. Crea el contenedor de dependencias que
 * comparten todas las pantallas. Firebase se inicializa automáticamente a
 * través del plugin `google-services` y `FirebaseInitProvider`.
 */
class MovChiaApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        // El modo demo (datos en memoria sin Firebase) solo existe en debug.
        container = DefaultAppContainer(this, allowDemoMode = BuildConfig.DEBUG)
    }
}
