package com.aj.geminiproj.ui.util

import android.util.Log
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

fun WindowSizeClass.isTablet(): Boolean {
    val result = this.widthSizeClass == WindowWidthSizeClass.Medium
            || this.widthSizeClass == WindowWidthSizeClass.Expanded
    return result
}

fun WindowSizeClass.isExpanded(): Boolean {
    return this.widthSizeClass == WindowWidthSizeClass.Expanded
}