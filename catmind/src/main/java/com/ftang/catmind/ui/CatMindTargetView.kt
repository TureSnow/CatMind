package com.ftang.catmind.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.ftang.catmind.extension.dpToPx
import com.ftang.catmind.extension.drawRect
import java.lang.ref.WeakReference

class CatMindTargetView(target : View?) {
    companion object {
        private val paint : Paint by lazy {
            Paint().apply {
                color = Color.RED
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2f.dpToPx
                pathEffect = DashPathEffect(floatArrayOf(2f.dpToPx, 2f.dpToPx), 0f)
            }
        }

        private val childPaint : Paint by lazy {
            Paint().apply {
                color = Color.parseColor("#8066B3FF")
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 2f.dpToPx
                pathEffect = DashPathEffect(floatArrayOf(2f.dpToPx, 2f.dpToPx), 0f)
            }
        }
    }

    private var target: WeakReference<View> = WeakReference(target)

    fun setTarget(view: View){
        this.target = WeakReference(view)
    }

    fun clearTarget() {
        this.target.clear()
    }

    private val location  = IntArray(2)

    fun draw(canvas: Canvas) {
        this.target.get()?.let { parent ->
            parent.getLocationOnScreen(location)
            canvas.drawRect(
                location[0],
                location[1],
                location[0] + parent.width,
                location[1] + parent.height,
                paint
            )
            if (parent is ViewGroup) {
                parent.children.forEach { child ->
                    child.getLocationOnScreen(location)
                    canvas.drawRect(
                        location[0],
                        location[1],
                        location[0] + child.width,
                        location[1] + child.height,
                        childPaint
                    )
                }
            }
        }
    }

}