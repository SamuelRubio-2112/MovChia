package com.movchia.app.ui.screens.cms_admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movchia.app.ui.components.LargeAccessibleButton
import com.movchia.app.ui.components.MovChiaTopBar
import com.movchia.app.ui.theme.ChiaBlue
import com.movchia.app.ui.theme.ChiaSkyBackground

/** Pantalla 07_LoginAdmin. */
@Composable
fun AdminLoginScreen(
    viewModel: AdminLoginViewModel,
    onBack: () -> Unit,
    onLoggedIn: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val demoMode by viewModel.demoMode.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.restoreSession(onSuccess = onLoggedIn) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { MovChiaTopBar(title = "Acceso al panel", onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier.size(96.dp).clip(CircleShape).background(ChiaSkyBackground),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = ChiaBlue, modifier = Modifier.size(44.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Panel de gestión", style = MaterialTheme.typography.headlineMedium, color = ChiaBlue)
            Text(
                "Secretaría de Movilidad de Chía",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))

            if (state.restoringEmail != null) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text(
                    "Reanudando la sesión de ${state.restoringEmail}…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                return@Column
            }

            if (demoMode) {
                Text(
                    "Modo demo activo: entra con cualquier correo y contraseña (rol administrador, sin Firebase).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Correo institucional") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            )
            Spacer(Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                LargeAccessibleButton(
                    text = "INGRESAR",
                    onClick = { viewModel.signIn(onSuccess = onLoggedIn) },
                    enabled = state.email.isNotBlank() && state.password.isNotBlank(),
                )
            }
            Spacer(Modifier.height(32.dp))
            Text(
                "Solo para personal de la Secretaría con rol asignado. Conexión cifrada con Firebase Authentication.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
