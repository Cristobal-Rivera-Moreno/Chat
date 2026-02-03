package com.cristobal.chat.utils

import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

object AppUtils {
    val isLandscape: Boolean
        @Composable get() =
            LocalConfiguration.current.orientation == ORIENTATION_LANDSCAPE
}