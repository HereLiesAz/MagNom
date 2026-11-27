package com.hereliesaz.magnom.ui

import androidx.compose.runtime.Composable

/**
 * Intercepts the system Back gesture/button while [enabled]. Android wires this to the
 * activity's back dispatcher; desktop has no system Back, so it does nothing there.
 */
@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
