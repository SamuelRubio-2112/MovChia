package com.movchia.app.utils

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.movchia.app.data.AppContainer
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.data.remote.FirebaseModule
import com.movchia.app.navigation.Screen
import com.movchia.app.navigation.homeScreen
import kotlinx.coroutines.launch

// =============================================================================
// SOLO PARA DESARROLLO. MainActivity lo muestra únicamente si BuildConfig.DEBUG.
// =============================================================================

/**
 * Botón flotante naranja con herramientas de desarrollo:
 *  - saltar entre las vistas de Niños, Jóvenes/Adultos y Adultos Mayores;
 *  - borrar la edad guardada para volver a la selección;
 *  - activar el Modo demo (datos en memoria, sin Firebase);
 *  - sembrar los datos de demostración en Firestore (requiere sesión admin).
 */
@Composable
fun DevAgeSwitcher(
    container: AppContainer,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val demoMode by container.demoMode.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var seeding by rememberSaveable { mutableStateOf(false) }

    fun goTo(screen: Screen) {
        navController.navigate(screen) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        expanded = false
    }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                // Abajo a la izquierda para no tapar los FAB y botones de enviar de la app.
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (expanded) {
                AgeGroup.entries.forEach { group ->
                    DevChip(text = group.displayName) {
                        container.ageGroupStore.save(group)
                        goTo(group.homeScreen())
                    }
                }
                DevChip(text = "Borrar edad guardada") {
                    container.ageGroupStore.clear()
                    goTo(Screen.AgeSelection)
                }
                DevChip(text = if (demoMode) "Modo demo: ACTIVO" else "Modo demo: apagado") {
                    container.setDemoMode(!demoMode)
                    toast(if (!demoMode) "Modo demo activado: datos en memoria, sin Firebase." else "Modo demo apagado: se usa Firebase.")
                    // Volver al Splash para que las pantallas recarguen con el nuevo origen de datos.
                    goTo(Screen.Splash)
                }
                DevChip(text = if (seeding) "Sembrando…" else "Sembrar datos en Firestore") {
                    if (seeding) return@DevChip
                    if (!container.authRepository.isStaffSignedIn) {
                        toast("Inicia sesión como administrador (Acceso Admin) antes de sembrar.")
                        return@DevChip
                    }
                    seeding = true
                    scope.launch {
                        runCatching { DevSeeder.seed(FirebaseModule.firestore) }
                            .onSuccess { toast("Listo: $it documentos escritos en Firestore.") }
                            .onFailure { toast("No se pudo sembrar: ${it.message}") }
                        seeding = false
                    }
                }
            }
            SmallFloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = if (demoMode) DevGreen else DevOrange,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Build, contentDescription = "Herramientas de desarrollo")
            }
        }
    }
}

@Composable
private fun DevChip(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = DevOrange, contentColor = Color.White),
    ) {
        Text(text)
    }
}

private val DevOrange = Color(0xFFF97316)
private val DevGreen = Color(0xFF16A34A)
