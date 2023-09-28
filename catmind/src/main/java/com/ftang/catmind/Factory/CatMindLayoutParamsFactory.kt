package com.ftang.catmind.Factory

import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import com.ftang.catmind.CatMind

object CatMindLayoutParamsFactory {
    const val FLOAT_TYPE = 1
    const val BOTTOM_TYPE = 2
    const val MASK_TYPE = 3
    fun create(paramsType: Int): LayoutParams {
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
            MASK_TYPE -> {
                val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    LayoutParams.TYPE_PHONE
                }
                return LayoutParams(type,
                    LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSPARENT
                )
            }

        }
        return LayoutParams()
    }
}
