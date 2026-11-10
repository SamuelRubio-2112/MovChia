package com.movchia.app.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.movchia.app.data.model.AgeGroup
import com.movchia.app.ui.theme.MovChiaTheme
import com.movchia.app.utils.Constants

/**
 * Botón de gran tamaño pensado para el modo Adultos Mayores ("Giant Buttons
 * List" en Figma), aunque se usa en toda la app como CTA principal.
 *
 * - Altura mínima de 72 dp (área táctil generosa).
 * - Texto en `titleMedium`, escalable con el ajuste del sistema.
 * - Icono opcional a la izquierda y otro a la derecha (flecha por defecto).
 * - Descripción accesible para lectores de pantalla.
 */
@Composable
fun LargeAccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    contentDescription: String = text,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Constants.SENIOR_BUTTON_MIN_HEIGHT_DP.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.width(12.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LargeAccessibleButtonPreview() {
    MovChiaTheme(ageGroup = AgeGroup.SENIORS) {
        LargeAccessibleButton(
            text = "VER MANUAL SIMPLE",
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
