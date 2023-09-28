package com.ftang.catmind.extension

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log

fun Application.registerPartialActivityLifecycleCallbacks(
    onActivityCreated: (activity: Activity) -> Unit,
    onActivityResumed: (activity: Activity) -> Unit,
    onActivityDestroyed: (activity: Activity) -> Unit
){
    this.registerActivityLifecycleCallbacks(object :
        Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.d("CatMind", "Application onCreate")
            onActivityCreated(activity)
        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {
            Log.d("CatMind", "Application onResume")
            onActivityResumed(activity)
        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            onActivityDestroyed(activity)
        }
    })
}

fun Class<out Any>.getClassNameWithExtension(): String {
    return if (this.isKotlin())
        this.simpleName + ".kt"
    else this.simpleName + ".java"
}

fun Class<out Any>.isKotlin() =
    this.declaredAnnotations.any { it.annotationClass == Metadata::class }