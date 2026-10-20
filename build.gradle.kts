// Archivo de build raíz del proyecto MovChia (Sistema ChíaMove).
// Los plugins se declaran aquí con `apply false` y se aplican en cada módulo.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.google.services) apply false
}
