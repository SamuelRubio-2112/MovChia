package com.movchia.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.movchia.app.data.model.AgeGroup

/**
 * Esquema institucional (Jóvenes/Adultos). Azul corporativo + turquesa sobre
 * fondos claros. Es el tema por defecto de la app.
 */
private val InstitutionalColorScheme: ColorScheme = lightColorScheme(
    primary = ChiaBlue,
    onPrimary = White,
    primaryContainer = ChiaSkyBackground,
    onPrimaryContainer = ChiaBlue,
    secondary = ChiaTeal,
    onSecondary = White,
    secondaryContainer = Color(0xFFCCF3F5),
    onSecondaryContainer = ChiaNavy,
    tertiary = ChiaYellow,
    onTertiary = ChiaNavy,
    tertiaryContainer = KidsBanner,
    onTertiaryContainer = Slate800,
    error = ChiaRed,
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = ChiaRed,
    background = Slate50,
    onBackground = Slate800,
    surface = White,
    onSurface = Slate800,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate200,
    outlineVariant = Slate100,
)

/**
 * Esquema Niños. Mismos colores institucionales pero con fondo cálido,
 * turquesa como primario y acentos vivos (verde, rosado, amarillo).
 */
private val KidsColorScheme: ColorScheme = InstitutionalColorScheme.copy(
    primary = ChiaTeal,
    onPrimary = White,
    primaryContainer = ChiaSky,
    onPrimaryContainer = White,
    secondary = ChiaGreen,
    onSecondary = ChiaNavy,
    secondaryContainer = ChiaGreen,
    onSecondaryContainer = White,
    tertiary = ChiaYellow,
    onTertiary = ChiaNavy,
    tertiaryContainer = ChiaPink,
    onTertiaryContainer = White,
    background = KidsBackground,
    onBackground = Slate800,
    surface = White,
    onSurface = Slate800,
)

/**
 * Esquema de ALTO CONTRASTE para Adultos Mayores (pantalla 06 en Figma):
 * fondo azul muy oscuro, texto blanco, botones amarillos y alerta roja.
 * Cumple relación de contraste AAA en los pares primario/onPrimario.
 */
private val HighContrastColorScheme: ColorScheme = darkColorScheme(
    primary = ChiaYellow,
    onPrimary = ChiaNavy,
    primaryContainer = ChiaYellow,
    onPrimaryContainer = ChiaNavy,
    secondary = White,
    onSecondary = ChiaNavy,
    secondaryContainer = ChiaNavy,
    onSecondaryContainer = White,
    tertiary = ChiaSkyLight,
    onTertiary = ChiaNavy,
    tertiaryContainer = Slate200,
    onTertiaryContainer = ChiaNavy,
    error = ChiaRed,
    onError = White,
    errorContainer = ChiaRed,
    onErrorContainer = White,
    background = ChiaBlueDark,
    onBackground = White,
    surface = ChiaNavy,
    onSurface = White,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate200,
    outline = White,
    outlineVariant = Slate400,
)

val MovChiaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Permite a cualquier composable saber si está en modo alto contraste. */
val LocalHighContrast = staticCompositionLocalOf { false }

/** Permite a cualquier composable conocer el grupo de edad activo. */
val LocalAgeGroup = staticCompositionLocalOf<AgeGroup?> { null }

/**
 * Tema raíz de MovChia.
 *
 * @param ageGroup grupo de edad activo; determina el esquema de color y la tipografía.
 * @param highContrast fuerza el esquema de alto contraste (por defecto se activa
 *   automáticamente en [AgeGroup.SENIORS]).
 *
 * El color dinámico de Android 12+ está deshabilitado intencionalmente para
 * respetar la identidad institucional de la Alcaldía de Chía.
 */
@Composable
fun MovChiaTheme(
    ageGroup: AgeGroup? = null,
    highContrast: Boolean = ageGroup == AgeGroup.SENIORS,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        highContrast -> HighContrastColorScheme
        ageGroup == AgeGroup.KIDS -> KidsColorScheme
        else -> InstitutionalColorScheme
    }
    val typography: Typography = if (highContrast) SeniorTypography else MovChiaTypography

    CompositionLocalProvider(
        LocalHighContrast provides highContrast,
        LocalAgeGroup provides ageGroup,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = MovChiaShapes,
            content = content,
        )
    }
}
