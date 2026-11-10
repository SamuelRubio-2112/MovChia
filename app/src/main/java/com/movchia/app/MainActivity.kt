package com.movchia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.movchia.app.navigation.AppNavigation
import com.movchia.app.ui.theme.MovChiaTheme
import com.movchia.app.utils.DevAgeSwitcher

/**
 * Única Activity de la app (single-activity + Navigation Compose).
 * El tema se recalcula automáticamente cuando cambia el rango de edad guardado.
 * Las herramientas de desarrollo ([DevAgeSwitcher]) solo se muestran en debug.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as MovChiaApplication).container

        setContent {
            val ageGroup by container.ageGroupStore.ageGroup.collectAsStateWithLifecycle()
            MovChiaTheme(ageGroup = ageGroup) {
                val navController = rememberNavController()
                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(container = container, navController = navController)
                    if (BuildConfig.DEBUG) {
                        DevAgeSwitcher(container = container, navController = navController)
                    }
                }
            }
        }
    }
}
