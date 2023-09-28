package com.ftang.catmind.util

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.Region
import android.os.Build

inline fun Canvas.difference (
    outRect: Rect,
    inRect: Rect,
    operation: Canvas.() -> Unit
) {
    val c = save()
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            clipRect(outRect)
            clipOutRect(inRect)
            operation()
        } else {
            clipRect(outRect)
            clipRect(inRect, Region.Op.DIFFERENCE)
            operation()
        }
    } finally {
        restoreToCount(c)
    }
}