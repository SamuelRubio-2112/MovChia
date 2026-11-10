package com.movchia.app.ui.screens.age_selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaPink
import com.movchia.app.ui.theme.ChiaTeal
import com.movchia.app.ui.theme.ChiaYellow
import com.movchia.app.ui.theme.White

/**
 * Rangos mostrados como chips. Un chip por grupo de edad: el Figma mostraba
 * 13-25 y 26-59 por separado, pero ambos llevan al mismo grupo (ADULTS) y se
 * marcaban seleccionados a la vez, lo que confundía al usuario.
 */
private data class AgeRangeChip(val label: String, val group: AgeGroup, val weight: Float = 1f)

private val ageRangeChips = listOf(
    AgeRangeChip("Niños\n0-12 años", AgeGroup.KIDS),
    // Etiqueta más larga: recibe más ancho para no partirse en tres líneas.
    AgeRangeChip("Jóvenes y Adultos\n13-59 años", AgeGroup.ADULTS, weight = 1.5f),
    AgeRangeChip("Mayores\n60+ años", AgeGroup.SENIORS),
)

@Composable
fun AgeSelectionScreen(
    viewModel: AgeSelectionViewModel,
    onContinue: (AgeGroup) -> Unit,
    onAdminAccess: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(onClick = onAdminAccess) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Acceso Admin", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(48.dp))
            BrandBanner()
            Spacer(Modifier.height(40.dp))

            Text(
                text = "¡Bienvenido a Chía!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Para adaptar los juegos y lecciones, dinos tu edad o selecciona un rango.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.ageInput,
                onValueChange = viewModel::onAgeInputChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ej. 18", style = MaterialTheme.typography.headlineMedium) },
                textStyle = MaterialTheme.typography.headlineMedium,
                singleLine = true,
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ageRangeChips.forEach { chip ->
                    val selected = state.ageInput.isEmpty() && state.selectedGroup == chip.group
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onGroupSelected(chip.group) },
                        label = {
                            Text(
                                text = chip.label,
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        },
                        modifier = Modifier.weight(chip.weight),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ChiaBlue,
                            selectedLabelColor = White,
                        ),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            LargeAccessibleButton(
                text = "COMENZAR EXPERIENCIA",
                onClick = { viewModel.confirm()?.let(onContinue) },
                enabled = state.canContinue,
                trailingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                containerColor = ChiaTeal,
                contentColor = White,
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Banner "Vías Seguras" con los tres círculos de la marca. */
@Composable
private fun BrandBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(ChiaBlue)
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text("Vías Seguras", style = MaterialTheme.typography.titleLarge, color = ChiaYellow)
            Text(
                "Aprende cultura vial de forma interactiva en nuestro municipio.",
                style = MaterialTheme.typography.bodySmall,
                color = White,
            )
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(ChiaPink))
            Box(Modifier.padding(start = 28.dp).size(44.dp).clip(CircleShape).background(ChiaTeal))
            Box(Modifier.padding(start = 56.dp).size(44.dp).clip(CircleShape).background(ChiaYellow))
        }
    }
}
