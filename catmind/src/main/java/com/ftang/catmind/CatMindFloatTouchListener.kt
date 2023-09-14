package com.ftang.catmind

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams

class CatMindFloatTouchListener(private val layoutParams: LayoutParams, private val windowManager: WindowManager): View.OnTouchListener {
    private var mX: Int = 0
    private var mY: Int = 0
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View?, motionEvent: MotionEvent?): Boolean {
        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                mX = motionEvent.rawX.toInt()
                mY = motionEvent.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val nowX = motionEvent.rawX.toInt()
                val nowY = motionEvent.rawY.toInt()
                val movedX = nowX - mX
                val movedY = nowY - mY
                mX = nowX
                mY = nowY
                layoutParams.apply {
                    x += movedX
                    y += movedY
                }
                //更新悬浮球控件位置
                windowManager.updateViewLayout(view, layoutParams)
            }
            else -> {

            }
        }
        return false
    }
}