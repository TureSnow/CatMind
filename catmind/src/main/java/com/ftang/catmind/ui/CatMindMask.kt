package com.ftang.catmind.ui

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ftang.catmind.CatMind
import com.ftang.catmind.service.CatMindWindowService
import com.ftang.catmind.touch.TouchTargetFinder
import com.ftang.catmind.ui.panel.CatMindPopupPanelContainerImpl
import com.ftang.catmind.util.fromLocation
import com.ftang.catmind.util.tryGetActivity
import java.lang.ref.WeakReference

/**
 * 用来拦截点击事件的一层mask
 */
class CatMindMask (
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val currentRootView by lazy(LazyThreadSafetyMode.NONE) {
        this.rootView
    }

    private val windowOffset by lazy(LazyThreadSafetyMode.NONE) {
        IntArray(2).apply {
            getLocationOnScreen(this)
        }
    }

    private var isSingleTap = false

    /**
     * 手势检测
     * 单击::绘制目标
     * 长按::退出catMind
     */
    private val gesture = GestureDetector(context,
        object : GestureDetector.SimpleOnGestureListener() {
            private var downEvent: MotionEvent? = null
            override fun onDown(e: MotionEvent): Boolean {
                downEvent?.recycle()
                downEvent = MotionEvent.obtain(e)
                return super.onDown(e)
            }

            override fun onSingleTapUp(e: MotionEvent): Boolean {
                isSingleTap = true
                return super.onSingleTapUp(e)
            }


            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                val activity = tryGetActivity(context)
                activity?.let {
                     downEvent?.fromLocation(windowOffset) {
                         val targetViews = TouchTargetFinder.findTouchTargetWithActivity(
                                 activity,
                                 downEvent!!,
                                 currentRootView
                             )
                         updateTargetViews(targetViews)
                         return true
                     }
                }
                return false
            }

            override fun onLongPress(e: MotionEvent) {
                val intent = Intent(CatMindWindowService.ACTION_LISTEN_TO_CAT_MIND).apply {
                    putExtra("type", CatMindWindowService.BROAD_CAST_TYPE_LONG_PRESS)
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                super.onLongPress(e)
            }
        })

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let {
            gesture.onTouchEvent(ev)
        }
        return super.dispatchTouchEvent(ev)
    }

    private var targetView: CatMindTargetView = CatMindTargetView(null)

    private val popupPanelContainer = CatMindPopupPanelContainerImpl(this)

    fun updateTargetViews(view: View?) {
        popupPanelContainer.dismiss()
        if (view == null) {
            targetView.clearTarget()
            CatMind.targetViewReference = null
        } else {
            if (CatMind.targetViewReference?.get() == view) {
                CatMind.targetViewReference = null
                targetView.clearTarget()
            } else {
                targetView.setTarget(view)
                CatMind.targetViewReference = WeakReference(view)
                popupPanelContainer.show(view)
            }
        }
        invalidate()
    }

    override fun dispatchDraw(canvas: Canvas?) {
        canvas?.let {
            canvas.save()
            try {
                canvas.translate(
                    -windowOffset[0].toFloat(),
                    -windowOffset[1].toFloat()
                )
                targetView.draw(canvas)
            }finally {
                canvas.restore()
            }
        }
        super.dispatchDraw(canvas)
    }

    override fun onDetachedFromWindow() {
        popupPanelContainer.dismiss()
        super.onDetachedFromWindow()
    }
}