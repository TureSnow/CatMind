package com.ftang.catmind

import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import android.widget.GridLayout

object CatMindLayoutParamsFactory {
    const val FLOAT_TYPE = 1
    const val BOTTOM_TYPE = 2
    const val CROSS_TYPE = 3
    const val BOUND_TYPE = 4
    fun createLayoutParams(paramsType: Int): LayoutParams {
        when (paramsType) {
            FLOAT_TYPE -> {
                return LayoutParams().apply {
                    type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        LayoutParams.TYPE_PHONE
                    }
                    format = PixelFormat.RGBA_8888
                    flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
                    width = ViewGroup.LayoutParams.WRAP_CONTENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    x = CatMind.floatX
                    y = CatMind.floatY
                }
            }

            BOTTOM_TYPE -> {
                return LayoutParams().apply {
                    type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        LayoutParams.TYPE_PHONE
                    }
                    format = PixelFormat.RGBA_8888
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    gravity = Gravity.BOTTOM
                    flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
                    x = 0
                    y = 0
                    windowAnimations = android.R.style.Animation_Dialog
                }
            }

            CROSS_TYPE -> {
                return LayoutParams().apply {
                    type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        LayoutParams.TYPE_PHONE
                    }
                    format = PixelFormat.RGBA_8888
                    width = ViewGroup.LayoutParams.WRAP_CONTENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    x = 0
                    y = 0
                    windowAnimations = android.R.style.Animation_Toast
                }
            }
            BOUND_TYPE -> {
                return LayoutParams().apply {
                    type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        LayoutParams.TYPE_APPLICATION_OVERLAY
                    } else {
                        LayoutParams.TYPE_PHONE
                    }
                    flags = LayoutParams.FLAG_NOT_TOUCH_MODAL or LayoutParams.FLAG_NOT_FOCUSABLE
                    format = PixelFormat.RGBA_8888
                    width = LayoutParams.MATCH_PARENT
                    height = LayoutParams.MATCH_PARENT
                    windowAnimations = android.R.style.Animation_Toast
                    gravity = Gravity.NO_GRAVITY
                }
            }
        }
        return LayoutParams()
    }
}
