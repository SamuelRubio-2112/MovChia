package com.movchia.app.utils

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.movchia.app.data.AppContainer
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.navigation.Screen
import com.movchia.app.navigation.homeScreen

// =============================================================================
// SOLO PARA PRUEBAS. Eliminar antes de publicar:
//   1. Borrar este archivo.
//   2. En MainActivity.kt quitar las líneas marcadas con "DEV".
// =============================================================================

/**
 * Botón flotante naranja que permite saltar entre las vistas de Niños,
 * Jóvenes/Adultos y Adultos Mayores, o borrar la edad guardada para volver a
 * la pantalla de selección. Se dibuja por encima de toda la navegación.
 */
@Composable
fun DevAgeSwitcher(
    container: AppContainer,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    fun goTo(screen: Screen) {
        navController.navigate(screen) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
        expanded = false
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.End,
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
            }
            SmallFloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = DevOrange,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Build, contentDescription = "Cambiar vista (solo pruebas)")
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
