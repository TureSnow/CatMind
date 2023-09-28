package com.ftang.catmind.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector

@SuppressLint("PrivateApi")
object WindowManagerUtil {

    val global by lazy(LazyThreadSafetyMode.NONE) {
        Class.forName("android.view.WindowManagerGlobal")
    }
    val windowGlobal : Any by lazy {
        global.getDeclaredMethod("getInstance").invoke(null)
    }

    val windowViews by lazy {
        val views = global.getDeclaredField("mViews")
        views.isAccessible = true //可以通过反射访问
        views
    }
    /**
     * 找到当前activity的view
     */
    fun findActivityViews(activity: Activity): List<View> {
        val allViews = findAllWindowViews()
        allViews?.let {
            return allViews.filter { view ->
                if (tryGetActivity(view.context) === activity //the special view add directly to windowManager
                    || view.context is Application //the system/application layer add directly to windowManager
                ) {
                    return@filter true
                } else {
                    val child = (view as ViewGroup).getChildAt(0)
                    child != null && tryGetActivity(child.context) === activity
                }
            }
        }
        return listOf(activity.window.decorView)
    }

    private var windowInspectorEnable = true

    /**
     * 获得该进程附着在window上的view
     */
    private fun findAllWindowViews(): List<View>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && windowInspectorEnable) {
            try {
                return WindowInspector.getGlobalWindowViews()
            } catch (e: Throwable) {
                windowInspectorEnable = false
            }
        }
        return try {
            windowViews.get(windowGlobal) as List<View>
        } catch (e: Throwable) {
            null
        }
    }
}