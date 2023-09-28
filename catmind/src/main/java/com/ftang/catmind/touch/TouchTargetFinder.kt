package com.ftang.catmind.touch

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.ftang.catmind.extension.isOnView
import com.ftang.catmind.util.WindowManagerUtil
import com.ftang.catmind.util.fromLocation
import com.ftang.catmind.util.toLocation
import java.lang.reflect.Field

@SuppressLint("DiscouragedPrivateApi", "PrivateApi")
object TouchTargetFinder {
    private val firstTouchTarget: Field by lazy(LazyThreadSafetyMode.NONE) {
        val f = ViewGroup::class.java.getDeclaredField("mFirstTouchTarget")
        f.isAccessible = true
        f
    }

    private val touchTargetChild: Field by lazy(LazyThreadSafetyMode.NONE) {
        val cls = Class.forName("android.view.ViewGroup\$TouchTarget")
        val f = cls.getDeclaredField("child")
        f.isAccessible = true
        f
    }

    private var getChildDrawingOrderNotFound = false
    private fun getChildDrawingOrder(parent: ViewGroup, idx: Int): Int {
        if (!getChildDrawingOrderNotFound && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                //todo: check isChildrenDrawingOrderEnabled
                val childIndex = parent.getChildDrawingOrder(idx)
                if (childIndex in 0 until parent.childCount) return childIndex
            } catch (e: Throwable) {
                getChildDrawingOrderNotFound = true
            }
        }
        return idx
    }
    private fun findFirstTouchTarget(
        view: View,
        event: MotionEvent
    ): View {
        var current = view
        while (true) {
            val next = findNextTouchTarget(current, event)
            if (next == null)
                return current
            else {
                current = next
            }
        }
    }

    private fun findNextTouchTarget(
        view: View,
        event: MotionEvent
    ): View? {
        if (view is ViewGroup) {
            return try {
                val touchTarget = firstTouchTarget.get(view)
                if (touchTarget != null) {
                    touchTargetChild.get(touchTarget) as? View
                } else {
                    findTouchTargetByEvent(view, event)
                }
            } catch (e: Throwable) {
                findTouchTargetByEvent(view, event)
            }
        }
        return null
    }

    private val location = IntArray(2)
    fun findTouchTargetWithActivity(
        activity: Activity,
        event: MotionEvent,
        maskView: View
    ): View? {
        obtainCancel(event.downTime) { cancel ->
            val findActivityViews = WindowManagerUtil.findActivityViews(activity)
            for (view in findActivityViews.asReversed()) {
                if (view == maskView) {
                    continue
                }
                view.getLocationOnScreen(location)
                var touchTarget: View? = null
                event.toLocation(location) {
                    if (isOnView(it, view)) {
                        view.dispatchTouchEvent(it)
                        touchTarget = findFirstTouchTarget(view, it)
                    }
                }
                cancel.toLocation(location) {
                    view.dispatchTouchEvent(it)
                }
                return touchTarget
            }

        }
        return null
    }
    private inline fun <T> obtainCancel(
        downTime: Long,
        action: (cancel: MotionEvent) -> T
    ): T {
        val cancel = MotionEvent.obtain(
            downTime,
            SystemClock.uptimeMillis(),
            MotionEvent.ACTION_CANCEL,
            0f,
            0f,
            0
        )

        return try {
            action(cancel)
        } finally {
            cancel.recycle()
        }
    }

    /**
     * 1. Sort the children in [parent] by the [ViewGroup.getChildDrawingOrder] and [View.getZ]
     * 2. Find the child who is on the top and is able to receive the [touchEvent]
     */
    private fun findTouchTargetByEvent(parent: ViewGroup, touchEvent: MotionEvent): View? {
        if (parent.childCount <= 0) return null

        val dispatchTouchOrder = ArrayList<View>(parent.childCount)
        for (drawIndex in 0 until parent.childCount) {
            val childIndex = getChildDrawingOrder(parent, drawIndex)
            val child = parent.getChildAt(childIndex)

            var insertIndex: Int = drawIndex
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // insert ahead of any Views with greater Z
                val currentZ = child.z
                while (insertIndex > 0 && dispatchTouchOrder[insertIndex - 1].z > currentZ) {
                    insertIndex--
                }
            }
            dispatchTouchOrder.add(insertIndex, child)
        }

        for (child in dispatchTouchOrder.asReversed()) { //from top to bottom
            if (child.visibility != View.GONE && isOnView(touchEvent, child)) {
                return child
            }
        }

        return null
    }

}