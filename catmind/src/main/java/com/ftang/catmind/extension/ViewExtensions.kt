package com.ftang.catmind.extension

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewParent
import android.view.WindowManager

fun View.findRootParent(): View {
    var current: View = this
    var next: ViewParent? = current.parent
    while (next is View) {
        current = next
        next = current.parent
    }
    return current
}

fun isOnView(e: MotionEvent, v: View): Boolean {
    val r = Rect()
    v.getGlobalVisibleRect(r)
    return r.left <= e.x && e.x <= r.right && r.top <= e.y && e.y <= r.bottom
}

fun Canvas.drawRect(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int,
    paint: Paint
) {
    this.drawRect(
        left.toFloat(),
        top.toFloat(),
        right.toFloat(),
        bottom.toFloat(),
        paint
    )
}