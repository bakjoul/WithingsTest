package com.bakjoul.testwithings.ui.snackbar

import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("SnackbarController not provided")
}
